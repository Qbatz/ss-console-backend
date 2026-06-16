package com.smartstay.console.services;

import com.smartstay.console.dao.SupportTicketNotes;
import com.smartstay.console.repositories.SupportTicketNotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportTicketNotesService {

    @Autowired
    private SupportTicketNotesRepository supportTicketNotesRepository;

    public SupportTicketNotes save(SupportTicketNotes supportTicketNotes) {
        return supportTicketNotesRepository.save(supportTicketNotes);
    }

    public List<SupportTicketNotes> getSupportTicketNotesByTicketId(Long supportTicketId) {
        return supportTicketNotesRepository.findAllByTicketIdOrderByIdDesc(supportTicketId);
    }
}
