package com.smartstay.console.repositories;

import com.smartstay.console.dao.HostelV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    List<HostelV1> findAllByParentIdIn(List<String> parentIds);

}
