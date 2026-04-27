package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerNotifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CustomerNotificationsRepository extends JpaRepository<CustomerNotifications, Long> {

    List<CustomerNotifications> findAllByUserIdIn(Set<String> userIds);

    List<CustomerNotifications> findAllByHostelId(String hostelId);
}
