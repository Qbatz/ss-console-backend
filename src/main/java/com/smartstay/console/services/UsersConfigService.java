package com.smartstay.console.services;

import com.smartstay.console.dao.UsersConfig;
import com.smartstay.console.repositories.UsersConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsersConfigService {

    @Autowired
    private UsersConfigRepository usersConfigRepository;

    public void save(UsersConfig usersConfig) {
        usersConfigRepository.save(usersConfig);
    }
}
