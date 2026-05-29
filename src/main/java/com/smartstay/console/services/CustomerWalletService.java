package com.smartstay.console.services;

import com.smartstay.console.dao.CustomerWallet;
import com.smartstay.console.repositories.CustomerWalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerWalletService {

    @Autowired
    private CustomerWalletRepository customerWalletRepository;

    public void saveAll(List<CustomerWallet> updatableCustomerWallets) {
        customerWalletRepository.saveAll(updatableCustomerWallets);
    }
}
