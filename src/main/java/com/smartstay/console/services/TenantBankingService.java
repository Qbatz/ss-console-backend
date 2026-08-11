package com.smartstay.console.services;

import com.smartstay.console.dao.TenantBanking;
import com.smartstay.console.repositories.TenantBankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantBankingService {

    @Autowired
    private TenantBankingRepository tenantBankingRepository;

    public List<TenantBanking> getByCustomerIds(List<String> customerIds) {
        return tenantBankingRepository.findAllByCustomerIdIn(customerIds);
    }

    public void deleteAll(List<TenantBanking> listTenantBankings) {
        tenantBankingRepository.deleteAll(listTenantBankings);
    }

    public List<TenantBanking> getByCustomerId(String customerId) {
        return tenantBankingRepository.findAllByCustomerId(customerId);
    }
}
