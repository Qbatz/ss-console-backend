package com.smartstay.console.repositories;

import com.smartstay.console.dao.SupportTicketNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketNotesRepository extends JpaRepository<SupportTicketNotes, Long> {

    List<SupportTicketNotes> findAllByTicketIdOrderByIdDesc(Long supportTicketId);
}
