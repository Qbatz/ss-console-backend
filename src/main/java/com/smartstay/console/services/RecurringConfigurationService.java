package com.smartstay.console.services;

import com.smartstay.console.dao.RecurringConfiguration;
import com.smartstay.console.repositories.RecurringConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecurringConfigurationService {

    @Autowired
    private RecurringConfigurationRepository recurringConfigurationRepository;

    public RecurringConfiguration getByHostelId(String hostelId) {
        return recurringConfigurationRepository.findByHostelId(hostelId);
    }

    public RecurringConfiguration save(RecurringConfiguration recurringConfiguration) {
        return recurringConfigurationRepository.save(recurringConfiguration);
    }
}
