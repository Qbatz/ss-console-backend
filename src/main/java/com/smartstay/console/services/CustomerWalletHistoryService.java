package com.smartstay.console.services;

import com.smartstay.console.dao.CustomerWalletHistory;
import com.smartstay.console.repositories.CustomersWalletHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomerWalletHistoryService {

    @Autowired
    private CustomersWalletHistoryRepository customersWalletHistoryRepository;

    public List<CustomerWalletHistory> findByCustomerIds(List<String> customerIds) {
        return customersWalletHistoryRepository.findByCustomerIdIn(customerIds);
    }

    public void deleteAll(List<CustomerWalletHistory> listCustomersWallet) {
        customersWalletHistoryRepository.deleteAll(listCustomersWallet);
    }

    public List<CustomerWalletHistory> findByCustomerId(String customerId) {
        return customersWalletHistoryRepository.findByCustomerId(customerId);
    }

    public List<CustomerWalletHistory> getWalletListForRecurring(List<String> customerIds) {
        return customersWalletHistoryRepository.findByCustomerIdIn(customerIds);
    }

    public void saveAll(List<CustomerWalletHistory> customerWallets) {
        customersWalletHistoryRepository.saveAll(customerWallets);
    }

    public  List<CustomerWalletHistory> getAllInvoiceNotGeneratedWallets(String customerId) {
        List<CustomerWalletHistory> listCustomerWalletHistory = customersWalletHistoryRepository
                .findInvoiceNotGeneratedByCustomerId(customerId);
        if (listCustomerWalletHistory == null) {
            listCustomerWalletHistory = new ArrayList<>();
        }

        return listCustomerWalletHistory;
    }

    public List<CustomerWalletHistory> getByInvoiceIds(Set<String> invoiceIds) {
        return customersWalletHistoryRepository.findAllByInvoiceIdIn(invoiceIds);
    }
}
