package com.smartstay.console.repositories;

import com.smartstay.console.dao.SupportTicketActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportTicketActivityRepository extends JpaRepository<SupportTicketActivity, Long> {
}
