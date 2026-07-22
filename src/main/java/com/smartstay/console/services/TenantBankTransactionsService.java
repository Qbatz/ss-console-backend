package com.smartstay.console.services;

import com.smartstay.console.dao.TenantBankTransactions;
import com.smartstay.console.repositories.TenantBankTransactionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantBankTransactionsService {

    @Autowired
    private TenantBankTransactionsRepository tenantBankTransactionsRepository;

    public List<TenantBankTransactions> getByCustomerIds(List<String> customerIds) {
        return tenantBankTransactionsRepository.findAllByCustomerIdIn(customerIds);
    }

    public void deleteAll(List<TenantBankTransactions> listTenantBankTransactions) {
        tenantBankTransactionsRepository.deleteAll(listTenantBankTransactions);
    }

    public List<TenantBankTransactions> getByCustomerId(String customerId) {
        return tenantBankTransactionsRepository.findAllByCustomerId(customerId);
    }
}
