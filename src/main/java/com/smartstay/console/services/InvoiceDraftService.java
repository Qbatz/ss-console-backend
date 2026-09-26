package com.smartstay.console.services;

import com.smartstay.console.dao.InvoiceDrafts;
import com.smartstay.console.repositories.InvoiceDraftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceDraftService {

    @Autowired
    private InvoiceDraftRepository invoiceDraftRepository;

    public InvoiceDrafts save(InvoiceDrafts invoiceDraft) {
        return invoiceDraftRepository.save(invoiceDraft);
    }
}
