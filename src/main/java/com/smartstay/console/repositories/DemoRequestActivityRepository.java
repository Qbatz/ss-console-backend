package com.smartstay.console.repositories;

import com.smartstay.console.dao.DemoRequestActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DemoRequestActivityRepository extends JpaRepository<DemoRequestActivity, Long> {

    List<DemoRequestActivity> findAllByRequestIdIn(Set<Long> demoRequestIds);
    @Query("""
            select count(a)
            from DemoRequestActivity a
            where a.createdAt >= :startDate and a.createdAt < :endDate
                and a.status = :status
            """)
    long getStatusCount(@Param("startDate") Date startDate,
                        @Param("endDate") Date endDate,
                        @Param("status") String status);

    List<DemoRequestActivity> findAllByRequestIdInOrderByActivityIdDesc(Set<Long> demoRequestIds);

    List<DemoRequestActivity> findAllByRequestIdOrderByActivityIdDesc(Long requestId);
}
