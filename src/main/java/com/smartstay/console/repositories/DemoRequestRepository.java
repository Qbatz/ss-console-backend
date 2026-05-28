package com.smartstay.console.repositories;

import com.smartstay.console.dao.DemoRequest;
import com.smartstay.console.dto.demoRequest.DemoRequestStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface DemoRequestRepository extends JpaRepository<DemoRequest, Long> {

    @Query("""
            select dr from DemoRequest dr
            where (:name is null or :name = '' or
                    lower(dr.name) like lower(concat('%', :name, '%')))
                and (:status is null or :status = '' or
                    dr.demoRequestStatus = :status)
                and (:agentId is null or :agentId = '' or
                    dr.assignedTo = :agentId)
                and (:startDate is null or dr.createdAt >= :startDate)
                and (:endDate is null or dr.createdAt < :endDate)
            order by dr.requestId desc
            """)
    Page<DemoRequest> findAllPaginated(@Param("name") String name,
                                       @Param("startDate") Date startDate,
                                       @Param("endDate") Date endDate,
                                       @Param("status") String status,
                                       @Param("agentId") String agentId,
                                       Pageable pageable);

    DemoRequest findByRequestId(Long demoRequestId);

    @Query("""
            select count(dr)
            from DemoRequest dr
            """)
    long getCount();

    @Query(value = """
            SELECT
                COUNT(*) AS totalLeads,
            
                COUNT(CASE
                    WHEN dr.created_at >= :todayStart
                     AND dr.created_at < :todayEnd
                    THEN 1
                END) AS todayNewCount,
            
                COUNT(CASE
                    WHEN dr.demo_request_status = 'NEW'
                    THEN 1
                END) AS newCount,
           
                COUNT(CASE
                    WHEN dr.demo_request_status = 'ASSIGNED'
                    THEN 1
                END) AS assignedCount,
            
                COUNT(CASE
                    WHEN dr.demo_request_status = 'CONTACTED'
                    THEN 1
                END) AS contactedCount,
            
                COUNT(CASE
                    WHEN dr.demo_request_status = 'DEMO_SCHEDULED'
                    THEN 1
                END) AS demoScheduledCount,
            
                COUNT(CASE
                    WHEN dr.demo_request_status = 'DEMO_COMPLETED'
                    THEN 1
                END) AS demoCompletedCount,
            
                COUNT(CASE
                    WHEN dr.demo_request_status = 'TRIAL_STARTED'
                    THEN 1
                END) AS trialStartedCount,
            
                COUNT(CASE
                    WHEN dr.demo_request_status = 'CONVERTED'
                    THEN 1
                END) AS convertedCount,
            
                COUNT(CASE
                    WHEN dr.demo_request_status = 'DROPPED'
                    THEN 1
                END) AS droppedCount
            
            FROM demo_request dr
            WHERE dr.created_at >= :monthStart
              AND dr.created_at < :monthEnd
            """, nativeQuery = true)
    DemoRequestStatsProjection getDashboardStats(@Param("todayStart") Date todayStart,
                                                 @Param("todayEnd") Date todayEnd,
                                                 @Param("monthStart") Date monthStart,
                                                 @Param("monthEnd") Date monthEnd);
}
