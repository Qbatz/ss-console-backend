package com.smartstay.console.services;

import com.smartstay.console.Mapper.users.OwnerDetailsMapper;
import com.smartstay.console.Mapper.users.OwnerListMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.hostelPlans.HostelPlanProjection;
import com.smartstay.console.dto.users.OwnerWithAddressProjection;
import com.smartstay.console.dto.users.UserSnapshot;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.OwnerSortField;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.owners.ResetPassword;
import com.smartstay.console.payloads.owners.UserEmailPayload;
import com.smartstay.console.payloads.owners.UserMobilePayload;
import com.smartstay.console.repositories.AgentRepository;
import com.smartstay.console.repositories.UsersRepository;
import com.smartstay.console.responses.users.OwnerDetailsResponse;
import com.smartstay.console.responses.users.OwnerResponse;
import com.smartstay.console.utils.Constants;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private UserActivitiesService userActivitiesService;
    @Autowired
    private HotelTypeService hotelTypeService;
    @Autowired
    private UserHostelService userHostelService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public ResponseEntity<?> resetPassword(ResetPassword resetPassword) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentRepository.findByAgentIdAndIsActiveTrue(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Owners.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (resetPassword == null) {
            return new ResponseEntity<>(Constants.PAYLOAD_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (resetPassword.password() == null) {
            return new ResponseEntity<>(Constants.PASSWORD_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (resetPassword.confirmPassword() == null) {
            return new ResponseEntity<>(Constants.CONFIRM_PASSWORD_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (resetPassword.userId() == null) {
            return new ResponseEntity<>(Constants.USER_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        Users users = usersRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(resetPassword.userId());
        if (users == null) {
            return new ResponseEntity<>(Constants.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        UserSnapshot oldUser = SnapshotUtility.toSnapshot(users);

        if (!resetPassword.password().equals(resetPassword.confirmPassword())){
            return new ResponseEntity<>(Constants.PASSWORD_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        String encodedPassword = encoder.encode(resetPassword.password());

        users.setPassword(encodedPassword);

        users = usersRepository.save(users);

        UserSnapshot newUser = SnapshotUtility.toSnapshot(users);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.RESET_PASSWORD,
                users.getUserId(), oldUser, newUser);

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

        List<OwnerWithAddressProjection> owners = usersRepository.findAllOwnersWithAddressProjection(name);

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

        Set<String> parentIds = owners.stream()
                .map(OwnerWithAddressProjection::getParentId)
                .collect(Collectors.toSet());

        List<HostelPlanProjection> hostels = hostelsService.getHostelPlanProjectionData(parentIds);

        Map<String, Integer> hostelCountMap = new HashMap<>();

        Map<String, Boolean> expiredOwnerMap = new HashMap<>();
        Map<String, Boolean> aboutToExpireOwnerMap = new HashMap<>();

        Date today = new Date();
        Date plus10 = Utils.addDaysToDate(today, 10);

        for (HostelPlanProjection hostel : hostels) {

            String parentId = hostel.parentId();
            Date end = hostel.currentPlanEndsAt();

            hostelCountMap.merge(parentId, 1, Integer::sum);

            if (end == null) {
                expiredOwnerMap.put(parentId, true);
                continue;
            }

            int cmpToday = Utils.compareWithTwoDates(end, today);
            int cmpPlus10 = Utils.compareWithTwoDates(end, plus10);

            if (cmpToday < 0) {
                expiredOwnerMap.put(parentId, true);
            }

            if (cmpToday >= 0 && cmpPlus10 < 0) {
                aboutToExpireOwnerMap.put(parentId, true);
            }
        }

        List<OwnerWithAddressProjection> filteredOwners = owners.stream()
                .filter(owner -> {

                    String parentId = owner.getParentId();

                    if (expired && expiredOwnerMap.getOrDefault(parentId, false)) {
                        return true;
                    }

                    if (aboutToExpire && aboutToExpireOwnerMap.getOrDefault(parentId, false)) {
                        return true;
                    }

                    return !expired && !aboutToExpire;
                })
                .toList();

        List<UserActivities> userActivitiesList =
                userActivitiesService.findLatestActivitiesByParentIds(parentIds);

        Map<String, UserActivities> userActivitiesMap = userActivitiesList.stream()
                .collect(Collectors.toMap(UserActivities::getParentId, ua -> ua));

        Comparator<OwnerWithAddressProjection> comparator;

        switch (sortField) {

            case OWNER_NAME -> comparator = Comparator.comparing(
                    u -> (u.getFirstName() + " " + u.getLastName()).toLowerCase()
            );

            case HOSTEL_COUNT -> comparator = Comparator.comparing(
                    u -> hostelCountMap.getOrDefault(u.getParentId(), 0)
            );

            case LATEST_ACTIVITY -> comparator = Comparator.comparing(
                    u -> {
                        UserActivities ua = userActivitiesMap.get(u.getParentId());
                        return ua != null ? ua.getCreatedAt() : null;
                    },
                    Comparator.nullsFirst(Comparator.naturalOrder())
            );

            default -> comparator = Comparator.comparing(OwnerWithAddressProjection::getCreatedAt);
        }

        if (isDesc) {
            comparator = comparator.reversed();
        }

        List<OwnerWithAddressProjection> sortedOwners = filteredOwners.stream()
                .sorted(comparator)
                .toList();

        int totalItems = sortedOwners.size();
        int fromIndex = Math.min(page * size, totalItems);
        int toIndex = Math.min(fromIndex + size, totalItems);
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<OwnerWithAddressProjection> pagedOwners = sortedOwners.subList(fromIndex, toIndex);

        List<OwnerResponse> ownersList = pagedOwners.stream()
                .map(owner -> new OwnerListMapper(
                        hostelCountMap.getOrDefault(owner.getParentId(), 0),
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

    public ResponseEntity<?> getOwnerById(String ownerId) {

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

        Users owner = usersRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(ownerId);
        if (owner == null){
            return new ResponseEntity<>(Utils.NO_OWNER_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<HostelV1> hostels = hostelsService.getHostelsByParentId(owner.getParentId());

        List<HotelType> hotelTypes = hotelTypeService.getAllHotelTypes();
        Map<Integer, HotelType> hotelTypeMap = hotelTypes.stream()
                .collect(Collectors.toMap(HotelType::getId, hotelType -> hotelType));

        List<UserActivities> userActivities = userActivitiesService.getActivitiesByUserId(ownerId);

        OwnerDetailsResponse response = new OwnerDetailsMapper(hostels, userActivities, hotelTypeMap)
                .apply(owner);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> updateOwnerEmailById(String ownerId, UserEmailPayload userEmailPayload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Owners.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Users owner = usersRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(ownerId);
        if (owner == null){
            return new ResponseEntity<>(Utils.NO_OWNER_FOUND, HttpStatus.BAD_REQUEST);
        }

        UserSnapshot oldOwner = SnapshotUtility.toSnapshot(owner);

        String newEmail = userEmailPayload.newEmail();

        if (usersRepository.existsByEmailIdAndUserIdNotAndIsActiveTrueAndIsDeletedFalse(newEmail, ownerId)) {
            return new ResponseEntity<>(Utils.EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        owner.setEmailId(newEmail);
        owner.setLastUpdate(new Date());

        owner = usersRepository.save(owner);

        UserSnapshot newOwner = SnapshotUtility.toSnapshot(owner);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.OWNERS,
                ownerId, oldOwner, newOwner);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public long getOwnerCount(){
        return usersRepository.getCount();
    }

    public ResponseEntity<?> deleteOwnerById(String ownerId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Owners.getId(), Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Users owner = usersRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(ownerId);
        if (owner == null){
            return new ResponseEntity<>(Utils.NO_OWNER_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (Utils.OWNER_ROLE_ID != owner.getRoleId()){
            return new ResponseEntity<>(Utils.THIS_USER_IS_NOT_AN_OWNER, HttpStatus.BAD_REQUEST);
        }

        UserSnapshot oldOwner = SnapshotUtility.toSnapshot(owner);

        List<HostelV1> hostels = hostelsService.getHostelsByParentId(owner.getParentId());

        if (!hostels.isEmpty()){
            return new ResponseEntity<>(Utils.HOSTELS_EXISTS_FOR_THIS_OWNER, HttpStatus.BAD_REQUEST);
        }

        List<UserHostel> userHostels = userHostelService.getUsersByParentId(owner.getParentId());

        if (!userHostels.isEmpty()){
            userHostelService.deleteAll(userHostels);
        }

        List<Users> users = usersRepository
                .findAllByParentIdAndIsActiveTrueAndIsDeletedFalse(owner.getParentId());

        if (!users.isEmpty()){
            usersRepository.deleteAll(users);
        }

        agentActivitiesService.createAgentActivity(agent, ActivityType.DELETE, Source.OWNERS,
                ownerId, oldOwner, null);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }

    public ResponseEntity<?> updateOwnerMobileById(String ownerId, UserMobilePayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Owners.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Users owner = usersRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(ownerId);
        if (owner == null){
            return new ResponseEntity<>(Utils.NO_OWNER_FOUND, HttpStatus.BAD_REQUEST);
        }

        UserSnapshot oldOwner = SnapshotUtility.toSnapshot(owner);

        String newMobileNo = payload.mobileNumber();

        if (usersRepository.existsByMobileNoAndUserIdNotAndIsActiveTrueAndIsDeletedFalse(newMobileNo, ownerId)) {
            return new ResponseEntity<>(Utils.MOBILE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        owner.setMobileNo(newMobileNo);
        owner.setLastUpdate(new Date());

        owner = usersRepository.save(owner);

        UserSnapshot newOwner = SnapshotUtility.toSnapshot(owner);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.OWNERS,
                ownerId, oldOwner, newOwner);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
