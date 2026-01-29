package com.smartstay.console.services;


import com.smartstay.console.Mapper.role.RolesMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.dao.RolesPermission;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.payloads.roles.AddRoles;
import com.smartstay.console.payloads.roles.Permission;
import com.smartstay.console.payloads.roles.UpdateRoles;
import com.smartstay.console.repositories.AgentRolesRepository;
import com.smartstay.console.responses.roles.Roles;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AgentRolesService {

    @Autowired
    private AgentRolesRepository agentRolesRepository;

    @Autowired
    private Authentication authentication;

    @Autowired
    private AgentService agentService;



    public ResponseEntity<?> addRole(AddRoles roleData) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Agent agent = agentService.findUserByUserId(userId);
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        AgentRoles rolesV1 = agentRolesRepository.findByRoleId(agent.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!checkPermission(agent.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (agentRolesRepository.existsByRoleName(roleData.roleName()) > 0) {
            return new ResponseEntity<>(Utils.ROLE_NAME_EXISTS, HttpStatus.BAD_REQUEST);
        }

        AgentRoles role = new AgentRoles();
        List<RolesPermission> rolesPermissions = permissionInsertion(roleData.permissionList());
        role.setCreatedAt(new Date());
        role.setUpdatedAt(new Date());
        role.setIsActive(true);
        role.setIsEditable(true);
        role.setIsDeleted(false);
        role.setRoleName(roleData.roleName());
        role.setPermissions(rolesPermissions);
        agentRolesRepository.save(role);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> updateRoleById(long roleId, UpdateRoles updatedRole) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Agent user = agentService.findUserByUserId(userId);
        AgentRoles rolesV1 = agentRolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!checkPermission(user.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        AgentRoles existingRole = agentRolesRepository.findByRoleId(roleId);
        if (existingRole == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }


        if (updatedRole.roleName() != null && !updatedRole.roleName().isEmpty()) {
            if (agentRolesRepository.existsByRoleNameNotRoleId(updatedRole.roleName(),roleId) > 0) {
                return new ResponseEntity<>(Utils.ROLE_NAME_EXISTS, HttpStatus.BAD_REQUEST);
            }
            existingRole.setRoleName(updatedRole.roleName());
        }
        if (updatedRole.isActive() != null) {
            existingRole.setIsActive(updatedRole.isActive());
        }
        if (updatedRole.permissionList() != null && !updatedRole.permissionList().isEmpty()) {
            Map<Integer, Permission> incomingPermissions = updatedRole.permissionList().stream().collect(Collectors.toMap(Permission::moduleId, Function.identity(), (a, b) -> b));

            List<RolesPermission> finalPermissions = Arrays.stream(ModuleId.values()).map(module -> updatePermission(module.getId(), incomingPermissions, existingRole.getPermissions())).collect(Collectors.toList());

            existingRole.setPermissions(finalPermissions);
        }
        existingRole.setUpdatedAt(new Date());
        agentRolesRepository.save(existingRole);
        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }

    public ResponseEntity<?> deleteRoleById(long roleId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Agent users = agentService.findUserByUserId(userId);
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!checkPermission(users.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (agentService.findActiveUsersByRoleId(roleId) != null && !agentService.findActiveUsersByRoleId(roleId).isEmpty()) {
            return new ResponseEntity<>(Utils.ACTIVE_USERS_FOUND, HttpStatus.BAD_REQUEST);
        }
        AgentRoles existingRole = agentRolesRepository.findByRoleId(roleId);
        if (existingRole != null) {
            existingRole.setIsDeleted(true);
            agentRolesRepository.save(existingRole);
            return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
        }
        return new ResponseEntity<>(Utils.NO_ROLES_FOUND, HttpStatus.BAD_REQUEST);

    }

    public ResponseEntity<?> getAllRoles() {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Agent agent = agentService.findUserByUserId(userId);
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        AgentRoles rolesV1 = agentRolesRepository.findByRoleId(agent.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!checkPermission(agent.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        List<AgentRoles> listRoles = agentRolesRepository.findAllByIsActiveTrueAndIsDeletedFalse();
        List<Roles> rolesList = listRoles.stream().map(item -> new RolesMapper().apply(item)).toList();
        return new ResponseEntity<>(rolesList, HttpStatus.OK);
    }

    public ResponseEntity<?> getRoleById(Long id) {
        if (id == null || id == 0) {
            return new ResponseEntity<>(Utils.INVALID_ROLE_ID, HttpStatus.NO_CONTENT);
        }
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Agent user = agentService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        AgentRoles rolesV1 = agentRolesRepository.findByRoleId(user.getRoleId());

        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!checkPermission(user.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        AgentRoles v1 = agentRolesRepository.findByRoleId(id);
        if (v1 != null) {
            Roles rolesData = new RolesMapper().apply(v1);
            return new ResponseEntity<>(rolesData, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.NO_ROLES_FOUND, HttpStatus.NO_CONTENT);

    }


    public boolean checkPermission(long roleId, int moduleId, String type) {
        AgentRoles roles = agentRolesRepository.findByRoleId(roleId);

        if (roles != null) {
            List<RolesPermission> rolesPermission = roles.getPermissions();
            if (!rolesPermission.isEmpty()) {
                List<RolesPermission> filteredPermission = rolesPermission.stream().filter(item -> item.getModuleId() == moduleId).toList();
                if (!filteredPermission.isEmpty()) {
                    if (type.equalsIgnoreCase(Utils.PERMISSION_READ)) {
                        return filteredPermission.get(0).isCanRead();
                    }
                    if (type.equalsIgnoreCase(Utils.PERMISSION_WRITE)) {
                        return filteredPermission.get(0).isCanWrite();
                    }
                    if (type.equalsIgnoreCase(Utils.PERMISSION_UPDATE)) {
                        return filteredPermission.get(0).isCanUpdate();
                    }
                    if (type.equalsIgnoreCase(Utils.PERMISSION_DELETE)) {
                        return filteredPermission.get(0).isCanDelete();
                    }
                }
            }
        }

        return false;
    }

    private List<RolesPermission> permissionInsertion(List<Permission> inputPermissions) {
        Map<Integer, Permission> permissionMap = inputPermissions.stream().collect(Collectors.toMap(Permission::moduleId, Function.identity(), (a, b) -> b));

        List<RolesPermission> result = new ArrayList<>();

        for (ModuleId module : ModuleId.values()) {
            Permission p = permissionMap.get(module.getId());
            RolesPermission rp = new RolesPermission();
            rp.setModuleId(module.getId());
            rp.setCanRead(p != null && Boolean.TRUE.equals(p.canRead()));
            rp.setCanWrite(p != null && Boolean.TRUE.equals(p.canWrite()));
            rp.setCanUpdate(p != null && Boolean.TRUE.equals(p.canUpdate()));
            rp.setCanDelete(p != null && Boolean.TRUE.equals(p.canDelete()));
            result.add(rp);
        }

        return result;
    }


    private RolesPermission updatePermission(int moduleId, Map<Integer, Permission> incomingPermissions, List<RolesPermission> existingPermissions) {
        Permission incoming = incomingPermissions.get(moduleId);

        RolesPermission existingDB = existingPermissions.stream().filter(p -> p.getModuleId() == moduleId).findFirst().orElse(new RolesPermission());

        RolesPermission merged = new RolesPermission();
        merged.setModuleId(moduleId);
        merged.setCanRead(incoming != null && incoming.canRead() != null ? incoming.canRead() : existingDB.isCanRead());
        merged.setCanWrite(incoming != null && incoming.canWrite() != null ? incoming.canWrite() : existingDB.isCanWrite());
        merged.setCanUpdate(incoming != null && incoming.canUpdate() != null ? incoming.canUpdate() : existingDB.isCanUpdate());
        merged.setCanDelete(incoming != null && incoming.canDelete() != null ? incoming.canDelete() : existingDB.isCanDelete());

        return merged;
    }
}
