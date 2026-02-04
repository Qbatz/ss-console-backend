package com.smartstay.console.services;

import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Users;
import com.smartstay.console.repositories.UsersRepository;
import com.smartstay.console.responses.hostels.OwnerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersRepository usersRepository;


    public List<Users> getOwners(List<String> parentId) {
        if (!authentication.isAuthenticated()) {
            return null;
        }

        return usersRepository.findOwners(parentId);
    }
}
