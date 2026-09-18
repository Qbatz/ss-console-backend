package com.smartstay.console.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.console.dao.UserActivities;
import com.smartstay.console.repositories.UserActivitiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class UserActivitiesService {

    @Autowired
    private UserActivitiesRepository userActivitiesRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<UserActivities> findLatestActivities(List<String> hostelIds) {
        return userActivitiesRepository.findLatestActivity(hostelIds);
    }

    public List<UserActivities> findLatestActivitiesByParentIds(Set<String> parentIds){
        return userActivitiesRepository.findLatestActivityPerParent(parentIds);
    }

    public List<UserActivities> getLimitedActivitiesByHostelId(String hostelId, int size){
        Pageable pageable = PageRequest.of(0, size);
        return userActivitiesRepository
                .findByHostelIdOrderByCreatedAtDesc(hostelId, pageable)
                .getContent();
    }

    public List<UserActivities> getLimitedActivitiesByUserId(String userId, int size){
        Pageable pageable = PageRequest.of(0, size);
        return userActivitiesRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .getContent();
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

    public List<UserActivities> getUserActivitiesByUserIds(Set<String> userIds){
        return userActivitiesRepository.findAllByUserIdInOrderByCreatedAtDesc(userIds);
    }

    public void deleteAll(List<UserActivities> userActivities) {
        userActivitiesRepository.deleteAll(userActivities);
    }

    public Page<UserActivities> getActivitiesByHostelIdAndBeforeDate(String hostelId, Date cutOffDate, Pageable pageable) {
        return userActivitiesRepository.findAllByHostelIdAndCreatedAtBefore(hostelId, cutOffDate, pageable);
    }

    public void restoreUserActivities(List<UserActivities> userActivities) {

        jdbcTemplate.batchUpdate("""
                INSERT INTO user_activities (
                    activity_id,
                    description,
                    user_id,
                    logged_at,
                    created_at,
                    parent_id,
                    source,
                    source_id,
                    activity_type,
                    hostel_id,
                    platform,
                    tenant_ids
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    activity_id = activity_id
                """,
                userActivities,
                userActivities.size(),
                (ps, activity) -> {

                    ps.setLong(1, activity.getActivityId());
                    ps.setString(2, activity.getDescription());
                    ps.setString(3, activity.getUserId());
                    ps.setObject(4, activity.getLoggedAt());
                    ps.setObject(5, activity.getCreatedAt());
                    ps.setString(6, activity.getParentId());
                    ps.setString(7, activity.getSource());
                    ps.setString(8, activity.getSourceId());
                    ps.setString(9, activity.getActivityType());
                    ps.setString(10, activity.getHostelId());
                    ps.setString(11, activity.getPlatform());

                    try {
                        ps.setString(
                                12,
                                objectMapper.writeValueAsString(
                                        activity.getTenantIds()
                                )
                        );
                    } catch (JsonProcessingException e) {
                        throw new SQLException("Failed to serialize tenantIds", e);
                    }
                });
    }
}
