package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.users.UserSnapshot;
import com.smartstay.console.dto.users.UsersNotesSnapshot;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.ennum.UserType;
import com.smartstay.console.payloads.users.UsersNotesPayload;
import com.smartstay.console.repositories.UsersRepository;
import com.smartstay.console.responses.users.UsersNotesResponse;
import com.smartstay.console.utils.Constants;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private UserActivitiesService userActivitiesService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private CustomerNotificationsService customerNotificationsService;
    @Autowired
    private LoginHistoryService loginHistoryService;
    @Autowired
    private TableColumnsService tableColumnsService;
    @Autowired
    private CommentsService commentsService;
    @Autowired
    private UsersConfigService usersConfigService;
    @Autowired
    private UserNotesService userNotesService;

    public List<Users> getOwners(List<String> parentId) {
        if (!authentication.isAuthenticated()) {
            return null;
        }

        return usersRepository.findOwners(parentId);
    }

    public Users getOwner(String parentId){
        return usersRepository.findOwner(parentId);
    }

    public List<Users> getMasters(HostelV1 hostel) {
        return usersRepository
                .findAllByParentIdAndRoleIdAndIsActiveTrueAndIsDeletedFalse(hostel.getParentId(), Utils.MASTER_ROLE_ID);
    }

    public List<Users> getStaffs(HostelV1 hostel) {
        Set<Integer> roleIds = Set.of(Utils.OWNER_ROLE_ID, Utils.MASTER_ROLE_ID);

        List<UserHostel> userHostels = userHostelService
                .getUsersByHostelId(hostel.getHostelId());

        Set<String> userIds = userHostels.stream()
                .map(UserHostel::getUserId)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        return usersRepository
                .findAllByParentIdAndRoleIdNotInAndUserIdInAndIsActiveTrueAndIsDeletedFalse(hostel.getParentId(), roleIds, userIds);
    }

    public List<Users> getStaffs(String parentId, Set<String> userIds){
        Set<Integer> roleIds = Set.of(Utils.OWNER_ROLE_ID, Utils.MASTER_ROLE_ID);

        return usersRepository
                .findAllByParentIdAndRoleIdNotInAndUserIdInAndIsActiveTrueAndIsDeletedFalse(parentId, roleIds, userIds);
    }

    public List<Users> getUsersByIds(Set<String> userIds){
        return usersRepository.findAllByUserIdInAndIsActiveTrueAndIsDeletedFalse(userIds);
    }

    public Users getUserById(String userId){
        return usersRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(userId);
    }

    public List<Users> getUsersByName(String name){
        return usersRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(name, name);
    }

    public void deleteAll(List<Users> users) {
        usersRepository.deleteAll(users);
    }

    public void deleteAllUserRelatedData(Set<String> userIds) {

        List<UserActivities> userActivities = userActivitiesService
                .getUserActivitiesByUserIds(userIds);
        List<AdminNotifications> adminNotifications = notificationService
                .getByUserIds(userIds);
        List<BankingV1> banks = bankingService
                .getByUserIds(userIds);
        List<Comments> comments = commentsService
                .getByUserIds(userIds);
        List<CustomerNotifications> customerNotifications = customerNotificationsService
                .getByUserIds(userIds);
        List<LoginHistory> loginHistories = loginHistoryService
                .getByUserIds(userIds);
        List<TableColumns> tableColumns = tableColumnsService
                .getByUserIds(userIds);

        userActivitiesService.deleteAll(userActivities);
        notificationService.deleteAll(adminNotifications);
        bankingService.deleteAll(banks);
        commentsService.deleteAll(comments);
        customerNotificationsService.deleteAll(customerNotifications);
        loginHistoryService.deleteAll(loginHistories);
        tableColumnsService.deleteAll(tableColumns);
    }

    public ResponseEntity<?> resetPinById(String userId) {

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

        Users users = usersRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(userId);
        if (users == null) {
            return new ResponseEntity<>(Constants.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        UsersConfig usersConfig = users.getConfig();
        if (usersConfig == null) {
            return new ResponseEntity<>(Constants.USER_CONFIG_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        UserSnapshot oldUser = SnapshotUtility.toSnapshot(users);

        usersConfig.setPin(null);
        usersConfigService.save(usersConfig);

        users.setLastUpdate(new Date());
        usersRepository.save(users);

        UserSnapshot newUser = SnapshotUtility.toSnapshot(users);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.RESET_USER_PIN,
                users.getUserId(), oldUser, newUser);

        return new ResponseEntity<>(Constants.UPDATED_SUCCESSFULLY, HttpStatus.OK);
    }

    public List<Users> getOwnersByMobileNo(String ownerMobile) {
        return usersRepository.findOwnersByMobileNo(ownerMobile);
    }

    public ResponseEntity<?> addUsersNotes(String userId, UsersNotesPayload payload) {

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

        Users users = usersRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(userId);
        if (users == null) {
            return new ResponseEntity<>(Constants.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        UsersNotes usersNotes = new UsersNotes();

        usersNotes.setComment(payload.notes());
        usersNotes.setCreatedByUserType(UserType.AGENT.name());
        usersNotes.setCreatedBy(authentication.getName());
        usersNotes.setCreatedAt(today);
        usersNotes.setUserId(userId);
        usersNotes.setParentId(users.getParentId());

        usersNotes = userNotesService.save(usersNotes);

        UsersNotesSnapshot newNotes = SnapshotUtility.toSnapshot(usersNotes);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.USERS_NOTES,
                String.valueOf(usersNotes.getId()), null, newNotes);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
    }

    public ResponseEntity<?> getUsersNotes(String userId) {

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

        Users users = usersRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(userId);
        if (users == null) {
            return new ResponseEntity<>(Constants.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<UsersNotes> usersNotes = userNotesService
                .getUserNotesByUserId(userId);

        Set<String> notesCreatedByAgentIds = new HashSet<>();
        for (UsersNotes note : usersNotes) {
            if (UserType.AGENT.name().equals(note.getCreatedByUserType())){
                notesCreatedByAgentIds.add(note.getCreatedBy());
            }
        }

        List<Agent> agents = agentService.getAgentsByIds(notesCreatedByAgentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a));

        List<UsersNotesResponse> response = usersNotes.stream()
                .map(notes -> {

                    String createdBy = null;
                    Agent createdByAgent = agentMap.getOrDefault(notes.getCreatedBy(), null);
                    if (createdByAgent != null){
                        createdBy = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
                    }

                    return new UsersNotesResponse(notes.getId(), notes.getComment(),
                            notes.getCreatedByUserType(), notes.getCreatedBy(), createdBy,
                            Utils.dateToString(notes.getCreatedAt()), Utils.dateToTime(notes.getCreatedAt()));
                }).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
