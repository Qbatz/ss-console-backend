package com.smartstay.console.services;

import com.smartstay.console.dao.TransactionV1;
import com.smartstay.console.repositories.TransactionV1Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionV1Service {
    @Autowired
    private TransactionV1Repository transactionV1Repository;


    public List<TransactionV1> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return transactionV1Repository.findByHostelIdAndCustomerIdIn(hostelId, customerIds);
    }

    public void deleteALl(List<TransactionV1> listTransactions) {
        transactionV1Repository.deleteAll(listTransactions);
    }
}
