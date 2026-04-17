package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.UserHostel;
import com.smartstay.console.dao.Users;
import com.smartstay.console.repositories.UsersRepository;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsersService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private UserHostelService userHostelService;


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

    public List<Users> getUsersByName(String name){
        return usersRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(name, name);
    }

    public void deleteAll(List<Users> users) {
        usersRepository.deleteAll(users);
    }
}
