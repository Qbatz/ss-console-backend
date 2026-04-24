package com.smartstay.console.services;

import com.smartstay.console.Mapper.orderHistory.OrderHistoryMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.OrderStatus;
import com.smartstay.console.ennum.UserType;
import com.smartstay.console.repositories.OrderHistoryRepository;
import com.smartstay.console.responses.orderHistory.OrderHistoryResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderHistoryService {

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private Authentication  authentication;
    @Autowired
    private HotelTypeService hotelTypeService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private PlansService plansService;
    @Autowired
    private UsersService usersService;

    public ResponseEntity<?> getOrderHistory(int page, int size, String name, Date startDate, Date endDate) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Payments.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Set<String> filteredHostelIds = new HashSet<>();
        Set<String> filteredUserIds = new HashSet<>();

        if (name != null && !name.trim().isEmpty()) {
            List<HostelV1> filteredHostels = hostelService.getHostelsByHostelName(name);
            filteredHostelIds = filteredHostels.stream()
                    .map(HostelV1::getHostelId)
                    .collect(Collectors.toSet());

            List<Users> filteredUsers = usersService.getUsersByName(name);
            filteredUserIds = filteredUsers.stream()
                    .map(Users::getUserId)
                    .collect(Collectors.toSet());
        }

        LocalDate today = LocalDate.now();

        if (startDate == null) {
            startDate = Utils.getStartDateOfMonth(today);
        }
        if (endDate == null) {
            endDate = Utils.getEndDateOfMonth(today);
        }
        endDate = Utils.addDaysToDate(endDate, 1);

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        List<String> orderStatuses = List.of(OrderStatus.PAID.name());

        double totalRevenue = orderHistoryRepository
                .findTotalRevenueBetween(startDate, endDate, orderStatuses);

        if (name != null && !name.trim().isEmpty()
                && filteredHostelIds.isEmpty() && filteredUserIds.isEmpty()) {

            return ResponseEntity.ok(Map.of(
                    "orderHistories", Collections.emptyList(),
                    "totalRevenue", totalRevenue,
                    "currentPage", page + 1,
                    "pageSize", size,
                    "totalItems", 0,
                    "totalPages", 0
            ));
        }

        Page<OrderHistory> paginatedOrderHistory;

        if (!filteredHostelIds.isEmpty() || !filteredUserIds.isEmpty()) {
            paginatedOrderHistory = orderHistoryRepository
                    .findFilteredOrderHistory(filteredHostelIds.isEmpty() ? null : filteredHostelIds,
                            filteredUserIds.isEmpty() ? null : filteredUserIds, startDate, endDate, pageable);
        } else {
            paginatedOrderHistory = orderHistoryRepository
                    .findAllByIsActiveTrueAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
                            startDate, endDate, pageable);
        }

        List<OrderHistory> orderHistories = paginatedOrderHistory.getContent();

        Set<String> hostelIds = new HashSet<>();
        Set<String> planCodes = new HashSet<>();
        Set<String> userIds = new HashSet<>();
        Set<String> agentIds = new HashSet<>();

        for (OrderHistory orderHistory : orderHistories) {
            if (orderHistory.getHostelId() != null) {
                hostelIds.add(orderHistory.getHostelId());
            }
            if (orderHistory.getPlanCode() != null) {
                planCodes.add(orderHistory.getPlanCode());
            }
            if (orderHistory.getPaidBy() != null) {
                userIds.add(orderHistory.getPaidBy());
            }
            if (orderHistory.getCollectedBy() != null) {
                agentIds.add(orderHistory.getCollectedBy());
            }
            if (orderHistory.getCreatedBy() != null && orderHistory.getUserType() != null) {
                if (UserType.OWNER.name().equals(orderHistory.getUserType())){
                    userIds.add(orderHistory.getCreatedBy());
                } else if (UserType.AGENT.name().equals(orderHistory.getUserType())) {
                    agentIds.add(orderHistory.getCreatedBy());
                }
            }
        }

        List<HotelType> hotelTypes = hotelTypeService.getAllHotelTypes();
        Map<Integer, HotelType> hotelTypeMap = hotelTypes.stream()
                .collect(Collectors.toMap(HotelType::getId, hotelType -> hotelType));

        List<HostelV1> hostels = hostelIds.isEmpty() ? Collections.emptyList() : hostelService.getHostelsByHostelIds(hostelIds);
        Map<String, HostelV1> hostelMap = hostels.stream()
                .collect(Collectors.toMap(HostelV1::getHostelId, hostel -> hostel, (a, b) -> a));

        List<Plans> plans = planCodes.isEmpty() ? Collections.emptyList() : plansService.findPlansByPlanCodes(planCodes);
        Map<String, Plans> plansMap = plans.stream()
                .collect(Collectors.toMap(Plans::getPlanCode, plan -> plan, (a, b) -> a));

        List<Users> usersList = userIds.isEmpty() ? Collections.emptyList() : usersService.getUsersByIds(userIds);
        Map<String, Users> usersMap = usersList.stream()
                .collect(Collectors.toMap(Users::getUserId, user -> user, (a, b) -> a));

        List<Agent> agents = agentIds.isEmpty() ? Collections.emptyList() : agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a, (a, b) -> a));

        List<OrderHistoryResponse> responseList = orderHistories.stream()
                .map(orderHistory -> {
                    HostelV1 hostel = hostelMap.getOrDefault(orderHistory.getHostelId(), null);
                    Plans plan = plansMap.getOrDefault(orderHistory.getPlanCode(), null);
                    HotelType hotelType = null;
                    if (hostel != null) {
                        hotelType = hotelTypeMap.getOrDefault(hostel.getHostelType(), null);
                    }
                    return new OrderHistoryMapper(hostel, hotelType,
                            plan, usersMap, agentMap).apply(orderHistory);
                }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("orderHistories", responseList);
        response.put("totalRevenue", totalRevenue);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", paginatedOrderHistory.getTotalElements());
        response.put("totalPages", paginatedOrderHistory.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public void save(OrderHistory newOrder) {
        orderHistoryRepository.save(newOrder);
    }
}
