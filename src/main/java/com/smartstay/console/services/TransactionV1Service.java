package com.smartstay.console.services;

import com.smartstay.console.dao.TransactionV1;
import com.smartstay.console.repositories.TransactionV1Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TransactionV1Service {

    @Autowired
    private TransactionV1Repository transactionV1Repository;

    public List<TransactionV1> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return transactionV1Repository.findByHostelIdAndCustomerIdIn(hostelId, customerIds);
    }

    public void deleteAll(List<TransactionV1> listTransactions) {
        transactionV1Repository.deleteAll(listTransactions);
    }

    public List<TransactionV1> findByHostelIdAndCustomerId(String hostelId, String customerId) {
        return transactionV1Repository.findByHostelIdAndCustomerId(hostelId, customerId);
    }

    public List<TransactionV1> getByInvoiceIds(Set<String> invoiceIds) {
        return transactionV1Repository.findAllByInvoiceIdIn(invoiceIds);
    }
}
