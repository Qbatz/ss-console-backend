package com.smartstay.console.repositories;

import com.smartstay.console.dao.UserActivities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivitiesRepository extends JpaRepository<UserActivities, Long> {

    @Query(value = """
            SELECT * FROM user_activities ua WHERE ua.created_at=(SELECT MAX(ua2.created_at) FROM user_activities ua2 
            WHERE ua2.hostel_id=ua.hostel_id) 
            AND ua.hostel_id in (:hostelIds)
            """, nativeQuery = true)
    List<UserActivities> findLatestActivity(@Param("hostelIds") List<String> hostelIds);
}
