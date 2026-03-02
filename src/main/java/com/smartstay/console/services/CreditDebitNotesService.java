package com.smartstay.console.services;

import com.smartstay.console.dao.CreditDebitNotes;
import com.smartstay.console.repositories.CreditDebitNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreditDebitNotesService {
    @Autowired
    private CreditDebitNoteRepository creditDebitNotesRepository;

    public List<CreditDebitNotes> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return creditDebitNotesRepository.findByHostelIdAndCustomerIds(hostelId, customerIds);
    }

    public void deleteAll(List<CreditDebitNotes> listCreditDebits) {
        creditDebitNotesRepository.deleteAll(listCreditDebits);
    }
}
