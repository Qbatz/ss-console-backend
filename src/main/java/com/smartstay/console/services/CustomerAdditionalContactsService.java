package com.smartstay.console.services;

import com.smartstay.console.dao.CustomerAdditionalContacts;
import com.smartstay.console.repositories.CustomerAdditionalContactsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerAdditionalContactsService {

    @Autowired
    private CustomerAdditionalContactsRepository customerAdditionalContactsRepository;

    public List<CustomerAdditionalContacts> findByHostelIdAndCustomerId(String hostelId, String customerId) {
        return customerAdditionalContactsRepository.findAllByHostelIdAndCustomerId(hostelId, customerId);
    }

    public void deleteAll(List<CustomerAdditionalContacts> listCustomerAdditionalContacts) {
        customerAdditionalContactsRepository.deleteAll(listCustomerAdditionalContacts);
    }

    public List<CustomerAdditionalContacts> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return customerAdditionalContactsRepository.findAllByHostelIdAndCustomerIdIn(hostelId, customerIds);
    }
}
