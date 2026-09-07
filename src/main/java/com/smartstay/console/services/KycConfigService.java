package com.smartstay.console.services;

import com.smartstay.console.dao.KycConfig;
import com.smartstay.console.repositories.KycConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KycConfigService {

    @Autowired
    private KycConfigRepository kycConfigRepository;

    public KycConfig getByHostelId(String hostelId) {
        return kycConfigRepository.findByHostelId(hostelId);
    }
}
