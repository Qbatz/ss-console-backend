package com.smartstay.console.services;

import com.smartstay.console.repositories.InvoiceItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceItemsService {

    @Autowired
    private InvoiceItemsRepository invoiceItemsRepository;
}
