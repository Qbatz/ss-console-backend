package com.smartstay.console.repositories;

import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.dao.HostelV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HostelPlanRepository extends JpaRepository<HostelPlan, Long> {
    @Query(value = """
            SELECT hp FROM HostelPlan hp WHERE hp.currentPlanEndsAt <= DATE(:todaysDate)
            """)
    List<HostelPlan> findNotActiveHostels(@Param("todaysDate") Date todaysDate);

    @Query(value = """
            SELECT * FROM hostel_plan ORDER BY current_plan_ends_at LIMIT :offset, :limit
            """, nativeQuery = true)
    List<HostelPlan> findAllHostelPlans(@Param("limit") int size, @Param("offset") int offset);

    @Query("""
            SELECT hp FROM HostelPlan hp WHERE DATE(hp.currentPlanEndsAt) >= DATE(:todaysDate)
            """)
    List<HostelPlan> findActiveHostels(@Param("todaysDate") Date todaysDate);

    List<HostelPlan> findAllByHostelIn(List<HostelV1> hostels);
}
