package com.smartstay.console.services;

import com.smartstay.console.dao.KycConfig;
import com.smartstay.console.repositories.KycConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class KycConfigService {

    @Autowired
    private KycConfigRepository kycConfigRepository;

    public KycConfig getByHostelId(String hostelId) {
        return kycConfigRepository.findByHostelId(hostelId);
    }

    public KycConfig save(KycConfig kycConfig) {
        return kycConfigRepository.save(kycConfig);
    }

    public List<KycConfig> getAllByHostelIds(Set<String> hostelIds) {
        return kycConfigRepository.findAllByHostelIdIn(hostelIds);
    }
}
