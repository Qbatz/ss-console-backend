package com.smartstay.console.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.console.Mapper.users.OwnerListMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.OwnerSortField;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.owners.ResetPassword;
import com.smartstay.console.repositories.AgentRepository;
import com.smartstay.console.repositories.UsersRepository;
import com.smartstay.console.responses.users.OwnerResponse;
import com.smartstay.console.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

//        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Subscriptions.getId(), Utils.PERMISSION_READ)) {
//            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
//        }

        Pageable pageable;

        OwnerSortField sortField = OwnerSortField.from(sortBy);

        String resolvedSortBy = sortField.getDbField();

        String finalDirection = "desc".equalsIgnoreCase(direction) ? "desc" : "asc";

        if (sortField == OwnerSortField.LATEST_ACTIVITY) {
            pageable = PageRequest.of(page, size);
        } else {
            Sort sort = finalDirection.equalsIgnoreCase("desc")
                    ? Sort.by(resolvedSortBy).descending()
                    : Sort.by(resolvedSortBy).ascending();

            pageable = PageRequest.of(page, size, sort);
        }

        Page<Users> pagedOwners;

        if (sortField == OwnerSortField.LATEST_ACTIVITY) {

            if ("desc".equalsIgnoreCase(finalDirection)) {
                pagedOwners = usersRepository.findAllOwnersOrderByLatestActivityDesc(name, pageable);
            } else {
                pagedOwners = usersRepository.findAllOwnersOrderByLatestActivityAsc(name, pageable);
            }

        } else {
            pagedOwners = usersRepository.findAllOwners(name, pageable);
        }

        List<Users> owners = pagedOwners.getContent();

        List<String> parentIds = owners.stream()
                .map(Users::getParentId).toList();

        List<HostelV1> hostels = hostelsService
                .getHostelsByParentIds(parentIds);

        Map<String, List<HostelV1>> hostelMap = hostels.stream()
                .collect(Collectors.groupingBy(HostelV1::getParentId));

        List<Address> addressList = addressService.getAddressByUsers(owners);

        Map<Users, Address> addressMap = addressList.stream()
                .collect(Collectors.toMap(
                        Address::getUser,
                        address -> address
                ));

        List<UserActivities> userActivitiesList = userActivitiesService
                .findLatestActivitiesByParentIds(parentIds);

        Map<String, UserActivities> userActivitiesMap = userActivitiesList.stream()
                .collect(Collectors.toMap(UserActivities::getParentId,
                        userActivities -> userActivities));

        List<OwnerResponse> ownersList = owners.stream()
                .map(owner -> new OwnerListMapper(
                        hostelMap.getOrDefault(owner.getParentId(), Collections.emptyList()).size(),
                        addressMap.get(owner),
                        hostelMap.getOrDefault(owner.getParentId(), Collections.emptyList()),
                        userActivitiesMap.get(owner.getParentId())
                ).apply(owner))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content", ownersList);
        response.put("currentPage", pagedOwners.getNumber());
        response.put("pageSize", size);
        response.put("totalItems", pagedOwners.getTotalElements());
        response.put("totalPages", pagedOwners.getTotalPages());
        response.put("sortBy", sortField.name());
        response.put("direction", finalDirection);

        List<Map<String, String>> sortOptions = List.of(
                Map.of("key", OwnerSortField.JOINING_DATE.name(), "label", "Joining Date"),
                Map.of("key", OwnerSortField.LATEST_ACTIVITY.name(), "label", "Latest Activity")
        );
        List<String> directionOptions = List.of("asc", "desc");

        response.put("availableSortBy", sortOptions);
        response.put("availableDirection", directionOptions);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
