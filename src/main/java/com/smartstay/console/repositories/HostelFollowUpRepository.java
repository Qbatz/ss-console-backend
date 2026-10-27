package com.smartstay.console.repositories;

import com.smartstay.console.dao.HostelFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface HostelFollowUpRepository extends JpaRepository<HostelFollowUp, Long> {

    HostelFollowUp findTopByHostelIdOrderByFollowUpIdDesc(String hostelId);

    List<HostelFollowUp> findAllByHostelIdOrderByFollowUpIdDesc(String hostelId);

    @Query(value = """
                    SELECT hf.*
                    FROM hostel_follow_up hf
                    JOIN (
                        SELECT hostel_id, MAX(follow_up_id) AS max_follow_up_id
                        FROM hostel_follow_up
                        WHERE hostel_id IN (:hostelIds)
                          AND status = :status
                        GROUP BY hostel_id
                    ) latest
                    ON hf.hostel_id = latest.hostel_id
                    AND hf.follow_up_id = latest.max_follow_up_id
                    """,
            nativeQuery = true)
    List<HostelFollowUp> findLatestByHostelIdsAndStatus(@Param("hostelIds") Set<String> hostelIds,
                                                        @Param("status") String status);

    @Query(value = """
                    SELECT hf.*
                    FROM hostel_follow_up hf
                    JOIN (
                        SELECT hostel_id, MAX(follow_up_id) AS max_follow_up_id
                        FROM hostel_follow_up
                        WHERE hostel_id IN (:hostelIds)
                        GROUP BY hostel_id
                    ) latest
                    ON hf.hostel_id = latest.hostel_id
                    AND hf.follow_up_id = latest.max_follow_up_id
                    """,
            nativeQuery = true)
    List<HostelFollowUp> findLatestByHostelIds(Set<String> hostelIds);
}
