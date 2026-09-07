package com.smartstay.console.services;

import com.smartstay.console.dao.KycHistory;
import com.smartstay.console.repositories.KycHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KycHistoryService {

    @Autowired
    private KycHistoryRepository kycHistoryRepository;

    public KycHistory getLatestByHostelId(String hostelId) {
        return kycHistoryRepository.findTopByHostelIdOrderByHistoryIdDesc(hostelId);
    }
}
