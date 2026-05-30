package com.smartstay.console.services;

import com.smartstay.console.Mapper.orderHistory.OrderHistoryMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.config.RestTemplateLoggingInterceptor;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.orderHistory.PaymentLinkGenerateDto;
import com.smartstay.console.dto.orderHistory.PaymentLinkGenerateResDto;
import com.smartstay.console.ennum.*;
import com.smartstay.console.payloads.orderHistory.PaymentLinkGeneratePayload;
import com.smartstay.console.payloads.orderHistory.PaymentLinkSharePayload;
import com.smartstay.console.repositories.OrderHistoryRepository;
import com.smartstay.console.responses.orderHistory.GeneratePaymentLinkRes;
import com.smartstay.console.responses.orderHistory.OrderHistoryResponse;
import com.smartstay.console.responses.orderHistory.VerifyResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

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
    @Autowired
    private WhatsappService whatsappService;

    @Value("${PAYMENT_URL}")
    private String paymentUrl;

    private final RestTemplate restTemplate;

    public OrderHistoryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

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

        Set<String> parentIds = hostels.stream()
                .map(HostelV1::getParentId)
                .collect(Collectors.toSet());
        List<Users> owners = parentIds.isEmpty() ? Collections.emptyList() : usersService.getOwners(new ArrayList<>(parentIds));
        Map<String, Users> ownerMap = owners.stream()
                .collect(Collectors.toMap(Users::getParentId, user -> user, (a, b) -> a));

        List<OrderHistoryResponse> responseList = orderHistories.stream()
                .map(orderHistory -> {
                    HostelV1 hostel = hostelMap.getOrDefault(orderHistory.getHostelId(), null);
                    Plans plan = plansMap.getOrDefault(orderHistory.getPlanCode(), null);
                    HotelType hotelType = null;
                    Users owner = null;
                    if (hostel != null) {
                        hotelType = hotelTypeMap.getOrDefault(hostel.getHostelType(), null);
                        owner = ownerMap.getOrDefault(hostel.getParentId(), null);
                    }
                    return new OrderHistoryMapper(hostel, hotelType,
                            plan, usersMap, agentMap, owner).apply(orderHistory);
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

    public ResponseEntity<?> verifyOrderHistory(Long orderHistoryId) {

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

        OrderHistory orderHistory = orderHistoryRepository.findByHistoryIdAndIsActiveTrue(orderHistoryId);
        if (orderHistory == null){
            return new ResponseEntity<>(Utils.ORDER_HISTORY_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        String paymentId = orderHistory.getPaymentId();

        if (paymentId == null) {
            return new ResponseEntity<>(Utils.PAYMENT_ID_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        VerifyResponse verifyResponse;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String verifyPaymentUrl = paymentUrl + "/v2/payments/" + paymentId ;

            restTemplate.exchange(
                    verifyPaymentUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            verifyResponse = new VerifyResponse(true, "SUCCESS");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            verifyResponse = new VerifyResponse(false, "FAILED");
        } catch (Exception e){
            return new ResponseEntity<>(Utils.UNABLE_TO_VERIFY_PAYMENT, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(verifyResponse, HttpStatus.OK);
    }

    public ResponseEntity<?> generatePaymentLink(String hostelId, PaymentLinkGeneratePayload payload) {

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

        HostelV1 hostel = hostelService.getHostelByHostelId(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        String planCode = payload.planCode();
        Plans plan = plansService.findPlanByPlanCode(planCode);
        if (plan == null){
            return new ResponseEntity<>(Utils.PLAN_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (PlanType.TRIAL.name().equals(plan.getPlanType()) ||
                PlanType.EXPANDABLE_TRIAL.name().equals(plan.getPlanType())) {
            return new ResponseEntity<>(Utils.INVALID_PLAN_CODE, HttpStatus.BAD_REQUEST);
        }

        double finalPrice = plan.getFinalPrice() != null ? plan.getFinalPrice() : 0;
        double discountAmount;

        try {
            discountAmount = Double.parseDouble(payload.discountAmount().toString());
        }
        catch (Exception e) {
            discountAmount = 0.0;
        }
        discountAmount = Utils.roundOfDoubleTo2Digits(discountAmount);

        if (discountAmount < 0 || discountAmount > finalPrice) {
            return new ResponseEntity<>(Utils.INVALID_DISCOUNT, HttpStatus.BAD_REQUEST);
        }

        double payableAmount = finalPrice - discountAmount;
        payableAmount = Utils.roundOfDoubleTo2Digits(payableAmount);

        try {
            String generatePaymentLink = paymentUrl + "/v2/payments/generate/" + hostelId ;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            PaymentLinkGenerateDto requestPayload = new PaymentLinkGenerateDto(payableAmount, "INR",
                    null, planCode, discountAmount, finalPrice, agent.getAgentId());

            HttpEntity<PaymentLinkGenerateDto> request =
                    new HttpEntity<>(requestPayload, headers);

            ResponseEntity<PaymentLinkGenerateResDto> response = restTemplate.exchange(
                    generatePaymentLink,
                    HttpMethod.POST,
                    request,
                    PaymentLinkGenerateResDto.class
            );

            PaymentLinkGenerateResDto responseBody = response.getBody();

            if (responseBody == null) {
                return new ResponseEntity<>(Utils.UNABLE_TO_GENERATE_PAYMENT_LINK, HttpStatus.BAD_REQUEST);
            }

            OrderHistory newOrder = new OrderHistory();
            newOrder.setHostelId(hostelId);
            newOrder.setPaymentUrl(responseBody.paymentLink());
            newOrder.setPaymentId(responseBody.paymentLinkId());
            newOrder.setPaymentLinkId(responseBody.paymentLinkId());
            newOrder.setDiscountAmount(discountAmount);
            newOrder.setPlanAmount(finalPrice);
            newOrder.setPlanCode(planCode);
            newOrder.setPlanName(plan.getPlanName());
            newOrder.setTotalAmount(payableAmount);
            newOrder.setOrderStatus(OrderStatus.CREATED.name());
            newOrder.setUserType(UserType.AGENT.name());
            newOrder.setActive(true);
            newOrder.setCreatedAt(new Date());
            newOrder.setCreatedBy(agent.getAgentId());

            orderHistoryRepository.save(newOrder);

            GeneratePaymentLinkRes paymentLinkRes = new GeneratePaymentLinkRes(responseBody.paymentLink());

            return new ResponseEntity<>(paymentLinkRes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Utils.UNABLE_TO_GENERATE_PAYMENT_LINK, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> sharePaymentLinkToWhatsapp(String hostelId, PaymentLinkSharePayload payload) {

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

        HostelV1 hostel = hostelService.getHostelByHostelId(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        String paymentLink = payload.paymentLink();

        OrderHistory orderHistory = orderHistoryRepository
                .findByPaymentUrlAndOrderStatusAndIsActiveTrue(paymentLink, OrderStatus.CREATED.name());
        if (orderHistory == null){
            return new ResponseEntity<>(Utils.ORDER_HISTORY_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!orderHistory.getHostelId().equals(hostelId)){
            return new ResponseEntity<>(Utils.PAYMENT_URL_AND_HOSTEL_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        String parentId = hostel.getParentId();

        Users owner = usersService.getOwner(parentId);
        if (owner == null){
            return new ResponseEntity<>(Utils.NO_OWNER_FOUND, HttpStatus.BAD_REQUEST);
        }

        String ownerName = Utils.getFullName(owner.getFirstName(), owner.getLastName());
        String ownerMobile = owner.getMobileNo();

        whatsappService.sendPaymentLink(ownerName, ownerMobile, paymentLink);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
