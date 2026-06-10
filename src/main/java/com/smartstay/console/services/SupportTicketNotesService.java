package com.smartstay.console.services;

import com.smartstay.console.repositories.SupportTicketNotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupportTicketNotesService {

    @Autowired
    private SupportTicketNotesRepository supportTicketNotesRepository;
}
