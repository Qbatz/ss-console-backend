package com.smartstay.console.services;

import com.smartstay.console.dao.InvoiceDiscounts;
import com.smartstay.console.repositories.InvoiceDiscountsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class InvoiceDiscountsService {

    @Autowired
    private InvoiceDiscountsRepository invoiceDiscountsRepository;

    public void deleteAll(List<InvoiceDiscounts> invoiceDiscounts) {
        invoiceDiscountsRepository.deleteAll(invoiceDiscounts);
    }

    public List<InvoiceDiscounts> getByInvoiceIds(Set<String> invoiceIds) {
        return invoiceDiscountsRepository.findAllByInvoiceIdIn(invoiceIds);
    }
}
