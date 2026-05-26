package com.smartstay.console.repositories;

import com.smartstay.console.dao.DemoRequest;
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

    @Query("""
            select count(dr)
            from DemoRequest dr
            where (:startDate is null or dr.createdAt >= :startDate)
                and (:endDate is null or dr.createdAt < :endDate)
            """)
    long getTotalLeadsCount(@Param("startDate") Date startDate,
                            @Param("endDate") Date endDate);

    @Query("""
            select count(dr)
            from DemoRequest dr
            where dr.createdAt >= :startDate
            and dr.createdAt < :endDate
            """)
    long getNewByDateCount(@Param("startDate") Date startDate,
                           @Param("endDate") Date endDate);
}
