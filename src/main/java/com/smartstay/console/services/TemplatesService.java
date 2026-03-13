package com.smartstay.console.services;

import com.smartstay.console.dao.BillTemplates;
import com.smartstay.console.repositories.BillTemplatesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplatesService {

    @Autowired
    BillTemplatesRepository billTemplatesRepository;

    public BillTemplates getTemplateByHostelId(String hostelId) {
        return billTemplatesRepository.getByHostelId(hostelId);
    }
}