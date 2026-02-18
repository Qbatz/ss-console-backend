package com.smartstay.console.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.console.Mapper.users.OwnerListMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.OwnerSortField;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.owners.ResetPassword;
import com.smartstay.console.repositories.AgentRepository;
import com.smartstay.console.repositories.UsersRepository;
import com.smartstay.console.responses.users.OwnerResponse;
import com.smartstay.console.utils.Constants;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OwnersService {

    @Autowired
    UsersRepository usersRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private HostelsService hostelsService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private UserActivitiesService userActivitiesService;
    @Autowired
    private HostelPlanService hostelPlanService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public ResponseEntity<?> resetPassword(ResetPassword resetPassword) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Agent agent = agentRepository.findByAgentId(authentication.getName());
        if (resetPassword == null) {
            return new ResponseEntity<>(Constants.PAYLOAD_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (resetPassword.password() == null) {
            return new ResponseEntity<>(Constants.PASSWORD_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (resetPassword.emailId() == null) {
            return new ResponseEntity<>(Constants.EMAIL_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        Users users = usersRepository.findByEmailId(resetPassword.emailId());
        if (users == null) {
            return new ResponseEntity<>(Constants.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Users oldUser = new ObjectMapper().convertValue(users, Users.class);

        String encodedPassword = encoder.encode(resetPassword.password());
        users.setPassword(encodedPassword);
        users = usersRepository.save(users);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.RESET_PASSWORD,
                users.getUserId(), oldUser, users);

        return new ResponseEntity<>(Constants.UPDATED_SUCCESSFULLY, HttpStatus.OK);

    }

    public ResponseEntity<?> getAllOwnersList(String name,
                                              Boolean isPropertiesExpired,
                                              Boolean isAboutToExpire,
                                              int page,
                                              int size,
                                              String sortBy,
                                              String direction) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Owners.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        OwnerSortField sortField = OwnerSortField.from(sortBy);
        String finalDirection = "desc".equalsIgnoreCase(direction) ? "desc" : "asc";

        boolean isDesc = "desc".equalsIgnoreCase(finalDirection);

        boolean expired = Boolean.TRUE.equals(isPropertiesExpired);
        boolean aboutToExpire = Boolean.TRUE.equals(isAboutToExpire);

        List<Users> owners = usersRepository.findAllOwners(name);

        List<Map<String, String>> sortOptions = Arrays.stream(OwnerSortField.values())
                .map(field -> Map.of(
                        "key", field.name(),
                        "label", field.getLabel()
                )).toList();

        if (owners.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "content", Collections.emptyList(),
                    "currentPage", page,
                    "pageSize", size,
                    "totalItems", 0,
                    "totalPages", 0,
                    "sortBy", sortField.name(),
                    "direction", finalDirection,
                    "availableSortBy", sortOptions,
                    "availableDirection", List.of("asc", "desc")
            ));
        }

        List<String> parentIds = owners.stream()
                .map(Users::getParentId)
                .toList();

        List<HostelV1> hostels = hostelsService.getHostelsByParentIds(parentIds);

        Map<String, List<HostelV1>> hostelMap = hostels.stream()
                .collect(Collectors.groupingBy(HostelV1::getParentId));

        List<HostelPlan> hostelPlans = hostelPlanService.getHostelPlansByHostels(hostels);

        Map<HostelV1, HostelPlan> hostelPlanMap = hostelPlans.stream()
                .collect(Collectors.toMap(HostelPlan::getHostel,
                        hostelPlan -> hostelPlan));

        List<Address> addressList = addressService.getAddressByUsers(owners);

        Map<Users, Address> addressMap = addressList.stream()
                .collect(Collectors.toMap(Address::getUser, address -> address));

        List<UserActivities> userActivitiesList =
                userActivitiesService.findLatestActivitiesByParentIds(parentIds);

        Map<String, UserActivities> userActivitiesMap = userActivitiesList.stream()
                .collect(Collectors.toMap(UserActivities::getParentId, ua -> ua));

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime plus10 = today.plusDays(10);

        List<Users> filteredOwners = owners.stream()
                .filter(owner -> {

                    List<HostelV1> ownerHostels =
                            hostelMap.getOrDefault(owner.getParentId(), Collections.emptyList());

                    if (expired || aboutToExpire) {

                        return ownerHostels.stream().anyMatch(h -> {
                            HostelPlan hp = hostelPlanMap.get(h);
                            if (hp == null || hp.getCurrentPlanEndsAt() == null) {
                                return expired;
                            }

                            LocalDateTime end = hp.getCurrentPlanEndsAt().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime();

                            if (expired && end.isBefore(today)) return true;

                            return aboutToExpire && (end.isAfter(today) && end.isBefore(plus10));
                        });
                    }

                    return true;
                }).toList();

        Comparator<Users> comparator;

        switch (sortField) {

            case OWNER_NAME -> comparator = Comparator.comparing(
                    u -> (u.getFirstName() + " " + u.getLastName()).toLowerCase()
            );

            case HOSTEL_COUNT -> comparator = Comparator.comparing(
                    u -> hostelMap.getOrDefault(u.getParentId(), Collections.emptyList()).size()
            );

            case LATEST_ACTIVITY -> comparator = Comparator.comparing(
                    u -> {
                        UserActivities ua = userActivitiesMap.get(u.getParentId());
                        return ua != null ? ua.getCreatedAt() : null;
                    },
                    Comparator.nullsFirst(Comparator.naturalOrder())
            );

            default -> comparator = Comparator.comparing(Users::getCreatedAt);
        }

        if (isDesc) {
            comparator = comparator.reversed();
        }

        List<Users> sortedOwners = filteredOwners.stream()
                .sorted(comparator)
                .toList();

        int totalItems = sortedOwners.size();
        int fromIndex = Math.min(page * size, totalItems);
        int toIndex = Math.min(fromIndex + size, totalItems);
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<Users> pagedOwners = sortedOwners.subList(fromIndex, toIndex);

        List<OwnerResponse> ownersList = pagedOwners.stream()
                .map(owner -> new OwnerListMapper(
                        hostelMap.getOrDefault(owner.getParentId(), Collections.emptyList()).size(),
                        addressMap.get(owner),
                        userActivitiesMap.get(owner.getParentId())
                ).apply(owner))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content", ownersList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        response.put("sortBy", sortField.name());
        response.put("direction", direction);
        response.put("availableSortBy", sortOptions);
        response.put("availableDirection", List.of("asc", "desc"));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
