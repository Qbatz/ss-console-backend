package com.smartstay.console.services;

import com.smartstay.console.dao.RecurringTracker;
import com.smartstay.console.repositories.RecurringTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RecurringTrackerService {

    @Autowired
    private RecurringTrackerRepository recurringTrackerRepository;

    public List<RecurringTracker> getLatestRecurringTrackersByHostelIds(Set<String> hostelIds) {
        return recurringTrackerRepository.getLatestRecurringTrackersByHostelIds(hostelIds);
    }
}
