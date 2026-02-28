package com.smartstay.console.services;

import com.smartstay.console.dao.InvoicesV1;
import com.smartstay.console.repositories.InvoiceV1Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceV1Service {

    @Autowired
    private InvoiceV1Repository invoiceV1Repository;
    public List<InvoicesV1> findByListOfCustomers(String hostelId, List<String> customerIds) {
        return invoiceV1Repository.findByHostelIdAndCustomerIdIn(hostelId, customerIds);
    }
}
