package com.smartstay.console.services;

import com.smartstay.console.dao.CustomerWalletHistory;
import com.smartstay.console.repositories.CustomerWalletHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class CustomerWalletHistoryService {

    @Autowired
    private CustomerWalletHistoryRepository customerWalletHistoryRepository;

    public void deleteAll(List<CustomerWalletHistory> cwhList) {
        customerWalletHistoryRepository.deleteAll(cwhList);
    }

    public List<CustomerWalletHistory> getByInvoiceIds(Set<String> invoiceIds) {
        return customerWalletHistoryRepository.findAllByInvoiceIdIn(invoiceIds);
    }
}
