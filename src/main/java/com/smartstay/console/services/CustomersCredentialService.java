package com.smartstay.console.services;

import com.smartstay.console.dao.CustomerCredentials;
import com.smartstay.console.repositories.CustomerCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class CustomersCredentialService {

    @Autowired
    private CustomerCredentialRepository customerCredentialRepository;

    public void deleteCredentials(List<CustomerCredentials> listCustomerCredentials) {
        customerCredentialRepository.deleteAll(listCustomerCredentials);
    }

    public void deleteCredential(CustomerCredentials customerCredentials) {
        customerCredentialRepository.delete(customerCredentials);
    }

    public CustomerCredentials findByXuid(String xuid) {
        return customerCredentialRepository.findByXuid(xuid);
    }

    public List<CustomerCredentials> findAllByXuids(Set<String> xuids) {
        return customerCredentialRepository.findAllByXuidIn(xuids);
    }
}
