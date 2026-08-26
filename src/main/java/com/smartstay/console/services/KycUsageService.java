package com.smartstay.console.services;

import com.smartstay.console.dao.KYCUsage;
import com.smartstay.console.repositories.KycUsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class KycUsageService {

    @Autowired
    private KycUsageRepository kycUsageRepository;

    public List<KYCUsage> getAllBetweenDates(Date startDate, Date endDate) {
        return kycUsageRepository.findAllByLatestRequestBetween(startDate, endDate);
    }

    public List<KYCUsage> getAllByCustomerIds(Set<String> tenantIds) {
        return kycUsageRepository.findAllByLatestRequestToIn(tenantIds);
    }
}
