package com.smartstay.console.services;

import com.smartstay.console.dao.SupportTicketActivity;
import com.smartstay.console.repositories.SupportTicketActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportTicketActivityService {

    @Autowired
    private SupportTicketActivityRepository supportTicketActivityRepository;

    public SupportTicketActivity save(SupportTicketActivity activity) {
        return supportTicketActivityRepository.save(activity);
    }

    public List<SupportTicketActivity> getAllByTicketId(Long supportTicketId) {
        return supportTicketActivityRepository.findAllByTicketIdOrderByActivityIdDesc(supportTicketId);
    }
}
