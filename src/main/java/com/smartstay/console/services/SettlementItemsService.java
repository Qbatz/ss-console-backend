package com.smartstay.console.services;

import com.smartstay.console.dao.SettlementItems;
import com.smartstay.console.repositories.SettlementItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class SettlementItemsService {

    @Autowired
    private SettlementItemsRepository settlementItemsRepository;

    public List<SettlementItems> getByInvoiceIds(Set<String> invoiceIds) {
        return settlementItemsRepository.findAllByInvoiceIdIn(invoiceIds);
    }

    public void deleteAll(List<SettlementItems> settlementItemsList) {
        settlementItemsRepository.deleteAll(settlementItemsList);
    }
}
