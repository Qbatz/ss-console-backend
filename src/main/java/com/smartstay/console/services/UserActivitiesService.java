package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.UserActivities;
import com.smartstay.console.repositories.UserActivitiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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

    public List<UserActivities> getActivitiesByHostelId(String hostelId){
        return userActivitiesRepository.findAllByHostelIdOrderByCreatedAtDesc(hostelId);
    }

    public List<UserActivities> getActivitiesByUserId(String userId){
        return userActivitiesRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<UserActivities> getPaginatedActivitiesByHostelId(String hostelId, Pageable pageable){
        return userActivitiesRepository.findByHostelIdOrderByCreatedAtDesc(hostelId, pageable);
    }

    public Page<UserActivities> getFilteredPaginatedActivitiesByHostelId(String hostelId,
                                                                         Set<String> userIds,
                                                                         Pageable pageable) {
        return userActivitiesRepository
                .findByHostelIdAndUserIdInOrderByCreatedAtDesc(hostelId, userIds, pageable);
    }
}
