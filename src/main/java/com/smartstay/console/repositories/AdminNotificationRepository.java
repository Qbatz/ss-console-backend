package com.smartstay.console.repositories;

import com.smartstay.console.dao.AdminNotifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotifications, Long> {

    List<AdminNotifications> findAllByUserIdIn(Set<String> userIds);

    List<AdminNotifications> findAllByHostelId(String hostelId);
}