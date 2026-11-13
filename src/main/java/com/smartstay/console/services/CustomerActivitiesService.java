package com.smartstay.console.services;

import com.smartstay.console.dao.CustomerActivities;
import com.smartstay.console.repositories.CustomerActivitiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerActivitiesService {

    @Autowired
    private CustomerActivitiesRepository customerActivitiesRepository;

    public List<CustomerActivities> getLimitedActivitiesByCustomerId(String customerId, int size) {

        Pageable pageable = PageRequest.of(0, size);

        return customerActivitiesRepository
                .findAllByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .getContent();
    }

    public Page<CustomerActivities> getPagedCustomerActivities(String customerId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return customerActivitiesRepository
                .findAllByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
    }
}
