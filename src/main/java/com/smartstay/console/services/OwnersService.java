package com.smartstay.console.services;

import com.smartstay.console.Mapper.users.OwnerDetailsMapper;
import com.smartstay.console.Mapper.users.OwnerHostelListResMapper;
import com.smartstay.console.Mapper.users.OwnerListMapper;
import com.smartstay.console.Mapper.users.UserOwnerInfoMapper;
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
import com.smartstay.console.repositories.UsersRepository;
import com.smartstay.console.responses.hostelRelationalAgent.HostelRelationalAgentResponse;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.responses.users.OwnerDetailsResponse;
import com.smartstay.console.responses.users.OwnerHostelListResponse;
import com.smartstay.console.responses.users.OwnerResponse;
import com.smartstay.console.responses.users.UserActivitiesResponse;
import com.smartstay.console.utils.Constants;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.UserActivityUtil;
import com.smartstay.console.utils.Utils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OwnersService {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private Authentication authentication;
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
    @Autowired
    private UsersService usersService;
    @Autowired
    private HostelRelationalAgentService hostelRelationalAgentService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private UserActivityUtil userActivityUtil;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public ResponseEntity<?> resetPassword(ResetPassword resetPassword) {

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
        users.setLastUpdate(new Date());

        users = usersRepository.save(users);

        UserSnapshot newUser = SnapshotUtility.toSnapshot(users);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.RESET_PASSWORD,
                users.getUserId(), oldUser, newUser);

        return new ResponseEntity<>(Constants.UPDATED_SUCCESSFULLY, HttpStatus.OK);
    }

    public ResponseEntity<?> getAllOwnersList(String name, Boolean isPropertiesExpired,
                                              Boolean isAboutToExpire, Boolean isActive,
                                              Boolean hasNoProperties, int page, int size,
                                              String sortBy, String direction) {

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
        boolean active = Boolean.TRUE.equals(isActive);
        boolean noProperties = Boolean.TRUE.equals(hasNoProperties);

        List<OwnerWithAddressProjection> owners = usersRepository.findAllOwnersWithAddressProjection(name);

        List<Map<String, String>> sortOptions = Arrays.stream(OwnerSortField.values())
                .map(field -> Map.of(
                        "key", field.name(),
                        "label", field.getLabel()
                )).toList();

        if (owners.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", Collections.emptyList());
            response.put("currentPage", page + 1);
            response.put("pageSize", size);
            response.put("totalItems", 0);
            response.put("totalPages", 0);
            response.put("ownersCount", 0);
            response.put("expiredCount", 0);
            response.put("aboutToExpireCount", 0);
            response.put("activeCount", 0);
            response.put("noPropertiesCount", 0);
            response.put("sortBy", sortField.name());
            response.put("direction", finalDirection);
            response.put("availableSortBy", sortOptions);
            response.put("availableDirection", List.of("asc", "desc"));

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Set<String> parentIds = owners.stream()
                .map(OwnerWithAddressProjection::getParentId)
                .collect(Collectors.toSet());

        List<HostelPlanProjection> hostels = hostelsService.getHostelPlanProjectionData(parentIds);

        Map<String, Integer> hostelCountMap = new HashMap<>();

        Set<String> expiredOwnerIds = new HashSet<>();
        Set<String> aboutToExpireOwnerIds = new HashSet<>();
        Set<String> activeOwnerIds = new HashSet<>();

        Date today = new Date();
        Date plus10 = Utils.addDaysToDate(today, 10);

        for (HostelPlanProjection hostel : hostels) {

            String parentId = hostel.parentId();
            Date end = hostel.currentPlanEndsAt();

            hostelCountMap.merge(parentId, 1, Integer::sum);

            if (end == null) {
                expiredOwnerIds.add(parentId);
                continue;
            }

            int cmpToday = Utils.compareWithTwoDates(end, today);
            int cmpPlus10 = Utils.compareWithTwoDates(end, plus10);

            if (cmpToday < 0) {
                expiredOwnerIds.add(parentId);
            }
            else if (cmpPlus10 < 0) {
                aboutToExpireOwnerIds.add(parentId);
                activeOwnerIds.add(parentId);
            }
            else {
                activeOwnerIds.add(parentId);
            }
        }

        Set<String> parentIdsWithHostels = hostelCountMap.keySet();

        long noPropertiesCount = owners.stream()
                .filter(owner -> !parentIdsWithHostels.contains(owner.getParentId()))
                .count();

        List<OwnerWithAddressProjection> filteredOwners = owners.stream()
                .filter(owner -> {

                    String parentId = owner.getParentId();

                    boolean hasNoHostels = !parentIdsWithHostels.contains(parentId);

                    if (expired && expiredOwnerIds.contains(parentId)) {
                        return true;
                    }

                    if (aboutToExpire && aboutToExpireOwnerIds.contains(parentId)) {
                        return true;
                    }

                    if (active && activeOwnerIds.contains(parentId)) {
                        return true;
                    }

                    if (noProperties && hasNoHostels) {
                        return true;
                    }

                    return !expired && !aboutToExpire && !active && !noProperties;
                }).toList();

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

        Set<String> pagedOwnersParentIds = pagedOwners.stream()
                .map(OwnerWithAddressProjection::getParentId)
                .collect(Collectors.toSet());

        List<HostelRelationalAgent> relationalAgents = hostelRelationalAgentService
                .getByParentIds(new HashSet<>(pagedOwnersParentIds));
        Map<String, List<HostelRelationalAgent>> relationalAgentMap = relationalAgents.stream()
                .collect(Collectors.groupingBy(HostelRelationalAgent::getParentId));

        Set<String> agentIds = new HashSet<>();
        for (HostelRelationalAgent relationalAgent : relationalAgents) {
            if (relationalAgent.getAgentId() != null){
                agentIds.add(relationalAgent.getAgentId());
            }
            if (relationalAgent.getCreatedBy() != null){
                agentIds.add(relationalAgent.getCreatedBy());
            }
        }

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId,
                        a -> a, (a, b) -> a));

        List<OwnerResponse> ownersList = pagedOwners.stream()
                .map(owner -> new OwnerListMapper(
                        hostelCountMap.getOrDefault(owner.getParentId(), 0),
                        userActivitiesMap.get(owner.getParentId()),
                        relationalAgentMap.getOrDefault(owner.getParentId(), null),
                        agentMap
                ).apply(owner))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content", ownersList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        response.put("ownersCount", owners.size());
        response.put("expiredCount", expiredOwnerIds.size());
        response.put("aboutToExpireCount", aboutToExpireOwnerIds.size());
        response.put("activeCount", activeOwnerIds.size());
        response.put("noPropertiesCount", noPropertiesCount);
        response.put("sortBy", sortField.name());
        response.put("direction", finalDirection);
        response.put("availableSortBy", sortOptions);
        response.put("availableDirection", List.of("asc", "desc"));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public void exportOwners(String name, Boolean isPropertiesExpired,
                             Boolean isAboutToExpire, Boolean isActive,
                             Boolean hasNoProperties, String sortBy,
                             String direction, HttpServletResponse response) throws IOException {

        if (!authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, Constants.UN_AUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, Constants.UN_AUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Owners.getId(), Utils.PERMISSION_READ)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, Utils.ACCESS_RESTRICTED);
        }

        List<OwnerResponse> ownersList = getDataForExport(name, isPropertiesExpired, isAboutToExpire,
                isActive, hasNoProperties, sortBy, direction);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Proprietors");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Client Name");
        header.createCell(1).setCellValue("Client Mobile No");
        header.createCell(2).setCellValue("Client Email Id");
        header.createCell(3).setCellValue("No Of Properties");
        header.createCell(4).setCellValue("Client Joined On");
        header.createCell(5).setCellValue("Client Last Activity Date");
        header.createCell(6).setCellValue("Client Last Activity Time");
        header.createCell(7).setCellValue("Relational Agent Name");
        header.createCell(8).setCellValue("Relational Agent Reason");
        header.createCell(9).setCellValue("Relational Agent Comments");

        int rowIdx = 1;
        for (OwnerResponse owner : ownersList) {
            String ownerName = null;
            if (owner.firstName() != null){
                ownerName = Utils.getFullName(owner.firstName(), owner.lastName());
            }

            HostelRelationalAgentResponse relationalAgentRes = null;
            if (owner.relationalAgents() != null && !owner.relationalAgents().isEmpty()){
                relationalAgentRes = owner.relationalAgents().getFirst();
            }

            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(ownerName != null ? ownerName : "");
            row.createCell(1).setCellValue(owner.mobileNo() != null ? owner.mobileNo() : "");
            row.createCell(2).setCellValue(owner.emailId() != null ? owner.emailId() : "");
            row.createCell(3).setCellValue(owner.noOfProperties());
            row.createCell(4).setCellValue(owner.joinedDate() != null ? owner.joinedDate() : "");
            row.createCell(5).setCellValue(owner.lastActivityDate() != null ? owner.lastActivityDate() : "");
            row.createCell(6).setCellValue(owner.lastActivityTime() != null ? owner.lastActivityTime() : "");
            row.createCell(7).setCellValue(relationalAgentRes != null ? relationalAgentRes.agentName() != null ? relationalAgentRes.agentName() : "" : "");
            row.createCell(8).setCellValue(relationalAgentRes != null ? relationalAgentRes.reason() != null ? relationalAgentRes.reason() : "" : "");
            row.createCell(9).setCellValue(relationalAgentRes != null ? relationalAgentRes.comments() != null ? relationalAgentRes.comments() : "" : "");
        }

        Row headerRow = sheet.getRow(0);
        int totalColumns = headerRow.getLastCellNum();

        for (int col = 0; col < totalColumns; col++) {
            sheet.autoSizeColumn(col);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"client.xlsx\"");

        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
        workbook.close();
    }

    private List<OwnerResponse> getDataForExport(String name, Boolean isPropertiesExpired,
                                                 Boolean isAboutToExpire, Boolean isActive,
                                                 Boolean hasNoProperties, String sortBy,
                                                 String direction) {

        OwnerSortField sortField = OwnerSortField.from(sortBy);
        String finalDirection = "desc".equalsIgnoreCase(direction) ? "desc" : "asc";

        boolean isDesc = "desc".equalsIgnoreCase(finalDirection);

        boolean expired = Boolean.TRUE.equals(isPropertiesExpired);
        boolean aboutToExpire = Boolean.TRUE.equals(isAboutToExpire);
        boolean active = Boolean.TRUE.equals(isActive);
        boolean noProperties = Boolean.TRUE.equals(hasNoProperties);

        List<OwnerWithAddressProjection> owners = usersRepository.findAllOwnersWithAddressProjection(name);

        if (owners.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> parentIds = owners.stream()
                .map(OwnerWithAddressProjection::getParentId)
                .collect(Collectors.toSet());

        List<HostelPlanProjection> hostels = hostelsService.getHostelPlanProjectionData(parentIds);

        Map<String, Integer> hostelCountMap = new HashMap<>();

        Set<String> expiredOwnerIds = new HashSet<>();
        Set<String> aboutToExpireOwnerIds = new HashSet<>();
        Set<String> activeOwnerIds = new HashSet<>();

        Date today = new Date();
        Date plus10 = Utils.addDaysToDate(today, 10);

        for (HostelPlanProjection hostel : hostels) {

            String parentId = hostel.parentId();
            Date end = hostel.currentPlanEndsAt();

            hostelCountMap.merge(parentId, 1, Integer::sum);

            if (end == null) {
                expiredOwnerIds.add(parentId);
                continue;
            }

            int cmpToday = Utils.compareWithTwoDates(end, today);
            int cmpPlus10 = Utils.compareWithTwoDates(end, plus10);

            if (cmpToday < 0) {
                expiredOwnerIds.add(parentId);
            }
            else if (cmpPlus10 < 0) {
                aboutToExpireOwnerIds.add(parentId);
                activeOwnerIds.add(parentId);
            }
            else {
                activeOwnerIds.add(parentId);
            }
        }

        Set<String> parentIdsWithHostels = hostelCountMap.keySet();

        List<OwnerWithAddressProjection> filteredOwners = owners.stream()
                .filter(owner -> {

                    String parentId = owner.getParentId();

                    boolean hasNoHostels = !parentIdsWithHostels.contains(parentId);

                    if (expired && expiredOwnerIds.contains(parentId)) {
                        return true;
                    }

                    if (aboutToExpire && aboutToExpireOwnerIds.contains(parentId)) {
                        return true;
                    }

                    if (active && activeOwnerIds.contains(parentId)) {
                        return true;
                    }

                    if (noProperties && hasNoHostels) {
                        return true;
                    }

                    return !expired && !aboutToExpire && !active && !noProperties;
                }).toList();

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

        Set<String> sortedOwnersParentIds = sortedOwners.stream()
                .map(OwnerWithAddressProjection::getParentId)
                .collect(Collectors.toSet());

        List<HostelRelationalAgent> relationalAgents = hostelRelationalAgentService
                .getByParentIds(new HashSet<>(sortedOwnersParentIds));
        Map<String, List<HostelRelationalAgent>> relationalAgentMap = relationalAgents.stream()
                .collect(Collectors.groupingBy(HostelRelationalAgent::getParentId));

        Set<String> agentIds = new HashSet<>();
        for (HostelRelationalAgent relationalAgent : relationalAgents) {
            if (relationalAgent.getAgentId() != null){
                agentIds.add(relationalAgent.getAgentId());
            }
            if (relationalAgent.getCreatedBy() != null){
                agentIds.add(relationalAgent.getCreatedBy());
            }
        }

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId,
                        a -> a, (a, b) -> a));

        return sortedOwners.stream()
                .map(owner -> new OwnerListMapper(
                        hostelCountMap.getOrDefault(owner.getParentId(), 0),
                        userActivitiesMap.get(owner.getParentId()),
                        relationalAgentMap.getOrDefault(owner.getParentId(), null),
                        agentMap
                ).apply(owner))
                .toList();
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

        List<UserActivities> userActivities = userActivitiesService
                .getLimitedActivitiesByUserId(ownerId, 50);

        Set<String> agentIds = new HashSet<>();

        List<HostelRelationalAgent> relationalAgents = hostelRelationalAgentService
                .getByParentId(owner.getParentId());
        for (HostelRelationalAgent relationalAgent : relationalAgents) {
            if (relationalAgent.getAgentId() != null){
                agentIds.add(relationalAgent.getAgentId());
            }
            if (relationalAgent.getCreatedBy() != null){
                agentIds.add(relationalAgent.getCreatedBy());
            }
        }

        List<Agent> agents = agentIds.isEmpty()
                ? Collections.emptyList()
                : agentService.getAgentsByIds(agentIds);

        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId,
                        agent1 -> agent1));

        List<UserActivitiesResponse> activitiesRes = userActivityUtil.buildResponses(userActivities);

        OwnerDetailsResponse response = new OwnerDetailsMapper(hostels, userActivities,
                activitiesRes, hotelTypeMap, relationalAgents, agentMap).apply(owner);

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

        List<HostelRelationalAgent> hostelRelationalAgents = hostelRelationalAgentService
                .getByParentId(owner.getParentId());

        if (!hostelRelationalAgents.isEmpty()){
            hostelRelationalAgentService.deleteAll(hostelRelationalAgents);
        }

        List<Users> users = usersRepository
                .findAllByParentIdAndIsActiveTrueAndIsDeletedFalse(owner.getParentId());

        if (!users.isEmpty()){

            Set<String> userIds = users.stream()
                    .map(Users::getUserId)
                    .collect(Collectors.toSet());

            usersService.deleteAllUserRelatedData(userIds);

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

    public ResponseEntity<?> getOwnerByMobile(String ownerMobile) {

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

        List<Users> owners = usersRepository.findOwnersByMobileNo(ownerMobile);

        List<OwnerInfo> ownerInfos = owners.stream()
                .map(owner -> new UserOwnerInfoMapper().apply(owner))
                .toList();

        return new ResponseEntity<>(ownerInfos, HttpStatus.OK);
    }

    public ResponseEntity<?> getOwnerByMobileNoOrName(String name) {

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

        List<Users> owners = usersRepository.findOwnersByMobileNoOrName(name);

        Set<String> parentIds = owners.stream()
                .map(Users::getParentId)
                .collect(Collectors.toSet());

        List<HostelV1> hostels = hostelService.getHostelsByParentIds(parentIds);

        Map<String, List<HostelV1>> hostelMap = hostels.stream()
                .collect(Collectors.groupingBy(HostelV1::getParentId));

        List<HotelType> hotelTypes = hotelTypeService.getAllHotelTypes();
        Map<Integer, HotelType> hotelTypeMap = hotelTypes.stream()
                .collect(Collectors.toMap(HotelType::getId, hotelType -> hotelType));

        List<UserHostel> userHostels = userHostelService.getUserHostelsByParentIds(parentIds);

        Map<String, List<UserHostel>> userHostelParentIdMap = userHostels.stream()
                .collect(Collectors.groupingBy(UserHostel::getParentId));

        Set<String> userIds = userHostels.stream()
                .map(UserHostel::getUserId)
                .collect(Collectors.toSet());

        List<Users> users = usersRepository.findAllByUserIdInAndIsActiveTrueAndIsDeletedFalse(userIds);

        Map<String, Users> usersMap = users.stream()
                .collect(Collectors.toMap(Users::getUserId, user -> user));

        List<OwnerHostelListResponse> responses = owners.stream()
                .map(owner -> {
                    List<HostelV1> ownerHostels = hostelMap.getOrDefault(owner.getParentId(), null);
                    List<UserHostel> hostelUsers = userHostelParentIdMap.getOrDefault(owner.getParentId(), null);
                    Map<String, Set<String>> hostelToUserIdsMap = null;
                    if (hostelUsers != null) {
                        hostelToUserIdsMap = hostelUsers.stream()
                                .collect(Collectors.groupingBy(
                                        UserHostel::getHostelId,
                                        Collectors.mapping(UserHostel::getUserId, Collectors.toSet())
                                ));
                    }
                    return new OwnerHostelListResMapper(ownerHostels, hotelTypeMap,
                            hostelToUserIdsMap, usersMap).apply(owner);
                }).toList();

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}
