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

    public List<SettlementItems> findByCustomerId(String customerId) {
        return settlementItemsRepository.findAllByCustomerId(customerId);
    }

    public List<SettlementItems> findByCustomerIds(Set<String> customerIdsSet) {
        return settlementItemsRepository.findAllByCustomerIdIn(customerIdsSet);
    }

    public SettlementItems getByInvoiceId(String invoiceId) {
        return settlementItemsRepository.findByInvoiceId(invoiceId);
    }

    public void save(SettlementItems settlementItems) {
        settlementItemsRepository.save(settlementItems);
    }
}
