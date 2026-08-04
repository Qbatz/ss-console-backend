package com.smartstay.console.services;

import com.smartstay.console.dao.InvoiceItems;
import com.smartstay.console.dao.InvoicesV1;
import com.smartstay.console.repositories.InvoiceItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceItemsService {

    @Autowired
    private InvoiceItemsRepository invoiceItemsRepository;

    public void deleteInvoiceItemsByInvoice(InvoicesV1 invoice){

        if (invoice == null){
            return;
        }

        List<InvoiceItems> invoiceItems = invoice.getInvoiceItems();

        invoiceItemsRepository.deleteAll(invoiceItems);
    }
}
