package com.smartstay.console.services;

import com.smartstay.console.dao.TrialDaysExtension;
import com.smartstay.console.repositories.TrialDaysExtensionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrialDaysExtensionService {

    @Autowired
    private TrialDaysExtensionRepository trialDaysExtensionRepository;

    public TrialDaysExtension save(TrialDaysExtension trialDaysExtension) {
        return trialDaysExtensionRepository.save(trialDaysExtension);
    }
}
