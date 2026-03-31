package com.smartstay.console.repositories;

import com.smartstay.console.dao.UserActivities;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserActivitiesRepository extends JpaRepository<UserActivities, Long> {

    @Query(value = """
                    SELECT ua.*
                    FROM user_activities ua
                    JOIN (
                        SELECT hostel_id, MAX(created_at) AS max_created_at
                        FROM user_activities
                        WHERE hostel_id IN (:hostelIds)
                        GROUP BY hostel_id
                    ) latest
                    ON ua.hostel_id = latest.hostel_id
                    AND ua.created_at = latest.max_created_at
            """, nativeQuery = true)
    List<UserActivities> findLatestActivity(@Param("hostelIds") List<String> hostelIds);

    @Query("""
            SELECT ua
            FROM UserActivities ua
            WHERE ua.activityId IN (
                SELECT MAX(u2.activityId)
                FROM UserActivities u2
                WHERE u2.parentId IN :parentIds
                GROUP BY u2.parentId
            )
            """)
    List<UserActivities> findLatestActivityPerParent(@Param("parentIds") Set<String> parentIds);

    Page<UserActivities> findByHostelIdOrderByCreatedAtDesc(String hostelId, Pageable pageable);

    List<UserActivities> findAllByUserIdOrderByCreatedAtDesc(String userId);

    Page<UserActivities> findByHostelIdAndUserIdInOrderByCreatedAtDesc(String hostelId, Set<String> userIds, Pageable pageable);
}
