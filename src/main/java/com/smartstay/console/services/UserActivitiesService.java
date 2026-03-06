package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.UserActivities;
import com.smartstay.console.repositories.UserActivitiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public List<UserActivities> findLatestActivitiesByParentIds(List<String> parentIds){
        return userActivitiesRepository.findLatestActivityPerParent(parentIds);
    }

    public List<UserActivities> getLimitedActivitiesByHostelId(String hostelId, int size){
        Pageable pageable = PageRequest.of(0, size);
        return userActivitiesRepository
                .findByHostelIdOrderByCreatedAtDesc(hostelId, pageable)
                .getContent();
    }

    public List<UserActivities> getActivitiesByUserId(String userId){
        return userActivitiesRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
}
