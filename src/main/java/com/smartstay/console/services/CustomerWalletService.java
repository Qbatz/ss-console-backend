package com.smartstay.console.services;

import com.smartstay.console.dao.CustomerWallet;
import com.smartstay.console.dao.CustomerWalletHistory;
import com.smartstay.console.repositories.CustomersWalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerWalletService {

    @Autowired
    private CustomersWalletRepository customersWalletRepository;
    public List<CustomerWalletHistory> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return customersWalletRepository.findByCustomerIdIn(customerIds);
    }

    public void deleteAll(List<CustomerWalletHistory> listCustomersWallet) {
        customersWalletRepository.deleteAll(listCustomersWallet);
    }
}
