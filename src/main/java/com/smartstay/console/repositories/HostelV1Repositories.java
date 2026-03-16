package com.smartstay.console.repositories;

import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dto.hostelPlans.HostelPlanProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface HostelV1Repositories extends JpaRepository<HostelV1, String> {
    @Query("""
            SELECT COUNT(h.hostelId) FROM hostelv1 h
            """)
    Long findHostelCount();

    @Query(value = """
            SELECT h.*
                FROM hostelv1 h
                LEFT JOIN hostel_plan hp ON h.hostel_id = hp.hostel_id
                WHERE (:name IS NULL OR LOWER(h.hostel_name) LIKE CONCAT('%', LOWER(:name), '%')) 
                ORDER BY hp.current_plan_ends_at LIMIT :offset, :limit
            """, nativeQuery = true)
    List<HostelV1> findAllHostels(@Param("limit") int size, @Param("offset") int offset, @Param("name") String name);

    @Query(value = """
        SELECT h.*
        FROM hostelv1 h
        LEFT JOIN hostel_plan hp ON h.hostel_id = hp.hostel_id
        WHERE (:name IS NULL OR LOWER(h.hostel_name) LIKE CONCAT('%', LOWER(:name), '%'))
        ORDER BY hp.current_plan_ends_at ASC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM hostelv1 h
        LEFT JOIN hostel_plan hp ON h.hostel_id = hp.hostel_id
        WHERE (:name IS NULL OR LOWER(h.hostel_name) LIKE CONCAT('%', LOWER(:name), '%'))
        """,
            nativeQuery = true)
    Page<HostelV1> findAllHostelsNew2(String name, Pageable pageable);

    @Query(value = """
        SELECT h.*
        FROM hostel_plan hp
        LEFT JOIN hostelv1 h ON h.hostel_id = hp.hostel_id
        WHERE (:name IS NULL OR LOWER(h.hostel_name) LIKE CONCAT('%', LOWER(:name), '%'))
        ORDER BY hp.current_plan_ends_at ASC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM hostelv1 h
        LEFT JOIN hostel_plan hp ON h.hostel_id = hp.hostel_id
        WHERE (:name IS NULL OR LOWER(h.hostel_name) LIKE CONCAT('%', LOWER(:name), '%'))
        """,
            nativeQuery = true)
    Page<HostelV1> findAllHostelsNew(String name, Pageable pageable);

    HostelV1 findByHostelId(String hostelId);

    List<HostelV1> findAllByHostelIdIn(Set<String> hostelIds);

    List<HostelV1> findByHostelNameContainingIgnoreCase(String hostelName);

    List<HostelV1> findAllByParentId(String parentId);

    @Query("""
            SELECT new com.smartstay.console.dto.hostelPlans.HostelPlanProjection(
                   h.parentId,
                   hp.currentPlanEndsAt
            )
            FROM hostelv1 h
            LEFT JOIN h.hostelPlan hp
            WHERE h.parentId IN :parentIds
            """)
    List<HostelPlanProjection> findHostelPlanProjectionData(@Param("parentIds") Set<String> parentIds);
}
