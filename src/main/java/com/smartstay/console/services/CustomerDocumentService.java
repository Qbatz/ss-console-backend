package com.smartstay.console.services;

import com.smartstay.console.dao.CustomerDocuments;
import com.smartstay.console.repositories.CustomerDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerDocumentService {
    @Autowired
    private CustomerDocumentRepository customerDocumentRepository;

    public List<CustomerDocuments> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return customerDocumentRepository.findByHostelIdAndCustomerIds(hostelId, customerIds);
    }
}
