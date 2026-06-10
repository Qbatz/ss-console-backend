package com.smartstay.console.repositories;

import com.smartstay.console.dao.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
}
