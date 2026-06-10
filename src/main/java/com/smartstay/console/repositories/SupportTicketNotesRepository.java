package com.smartstay.console.repositories;

import com.smartstay.console.dao.SupportTicketNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportTicketNotesRepository extends JpaRepository<SupportTicketNotes, Long> {
}
