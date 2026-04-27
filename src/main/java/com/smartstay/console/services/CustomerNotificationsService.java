package com.smartstay.console.services;

import com.smartstay.console.dao.CustomerNotifications;
import com.smartstay.console.repositories.CustomerNotificationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class CustomerNotificationsService {

    @Autowired
    private CustomerNotificationsRepository customerNotificationsRepository;

    public void deleteAll(List<CustomerNotifications> customerNotifications) {
        customerNotificationsRepository.deleteAll(customerNotifications);
    }

    public List<CustomerNotifications> getByUserIds(Set<String> userIds) {
        return customerNotificationsRepository.findAllByUserIdIn(userIds);
    }

    public List<CustomerNotifications> findByHostelId(String hostelId) {
        return customerNotificationsRepository.findAllByHostelId(hostelId);
    }
}
