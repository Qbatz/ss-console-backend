package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.UserHostel;
import com.smartstay.console.dao.Users;
import com.smartstay.console.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
        return usersRepository.findAllByParentIdAndRoleId(hostel.getParentId(), 2);
    }

    public List<Users> getStaffs(HostelV1 hostel) {
        Set<Integer> roleIds = Set.of(1,2);

        List<UserHostel> userHostels = userHostelService
                .getUsersByHostelId(hostel.getHostelId());

        List<String> userIds = userHostels.stream()
                .map(UserHostel::getUserId)
                .toList();

        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        return usersRepository
                .findAllByParentIdAndRoleIdNotInAndUserIdIn(hostel.getParentId(), roleIds, userIds);
    }
}
