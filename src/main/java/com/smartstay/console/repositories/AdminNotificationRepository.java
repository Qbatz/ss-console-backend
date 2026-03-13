package com.smartstay.console.repositories;

import com.smartstay.console.dao.AdminNotifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotifications, Long> {
}