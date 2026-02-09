package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.UserActivities;
import com.smartstay.console.repositories.UserActivitiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserActivitiesService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private UserActivitiesRepository userActivitiesRepository;

    public List<UserActivities> findLatestActivities(List<String> hostelIds) {
        return userActivitiesRepository.findLatestActivity(hostelIds);
    }
}
