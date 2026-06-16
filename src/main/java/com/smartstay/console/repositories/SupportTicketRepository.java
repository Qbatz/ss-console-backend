package com.smartstay.console.repositories;

import com.smartstay.console.dao.SupportTicket;
import com.smartstay.console.dto.supportTicket.SupportTicketStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    @Query("""
           SELECT st
           FROM SupportTicket st
           WHERE st.ticketNumber LIKE CONCAT('ST-', :year, '-%')
           ORDER BY st.ticketId DESC
           LIMIT 1
           """)
    Optional<SupportTicket> findLatestTicketForYear(@Param("year") int year);

    @Query(value = """
            select
                count(*) as totalLeads,
                count(case
                        when st.created_at >= :todayStart
                            and st.created_at < :todayEnd
                        then 1
                      end) as todayNewCount,
                count(case
                        when st.ticket_status = 'WAITING'
                        then 1
                      end) as waitingCount,
                count(case
                        when st.ticket_status = 'ASSIGNED'
                        then 1
                      end) as assignedCount,
                count(case
                        when st.ticket_status = 'IN_PROGRESS'
                        then 1
                      end) as inProgressCount,
                count(case
                        when st.ticket_status = 'RESOLVED'
                        then 1
                      end) as resolvedCount,
                count(case
                        when st.ticket_status = 'CLOSED'
                        then 1
                      end) as closedCount
            from support_ticket st
            where st.created_at >= :monthStart
                and st.created_at < :monthEnd
            """, nativeQuery = true)
    SupportTicketStatsProjection getDashboardStats(@Param("todayStart") Date todayStart,
                                                   @Param("todayEnd") Date todayEnd,
                                                   @Param("monthStart") Date monthStart,
                                                   @Param("monthEnd") Date monthEnd);

    @Query("""
            select st from SupportTicket st
            where (:ticketIds is null or st.ticketId in :ticketIds)
                and (:status is null or st.ticketStatus = :status)
                and (:agentId is null or st.assignedTo = :agentId)
                and (:startDate is null or st.createdAt >= :startDate)
                and (:endDate is null or st.createdAt < :endDate)
            order by st.ticketId desc
            """)
    Page<SupportTicket> findAllPaginated(@Param("ticketIds") Set<Long> ticketIds,
                                         @Param("startDate") Date startDate,
                                         @Param("endDate") Date endDate,
                                         @Param("status") String status,
                                         @Param("agentId") String agentId,
                                         Pageable pageable);

    List<SupportTicket> findAllByHostelIdIn(Set<String> hostelIds);

    List<SupportTicket> findByTicketNumberContainingIgnoreCase(String name);

    SupportTicket findByTicketId(Long ticketId);
}
