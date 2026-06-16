package com.smartstay.console.services;

import com.smartstay.console.dao.HostelNotes;
import com.smartstay.console.repositories.HostelNotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostelNotesService {

    @Autowired
    private HostelNotesRepository hostelNotesRepository;

    public List<HostelNotes> getAllByHostelId(String hostelId){
        return hostelNotesRepository.findAllByHostelIdOrderByIdDesc(hostelId);
    }

    public HostelNotes save(HostelNotes hostelNotes){
        return hostelNotesRepository.save(hostelNotes);
    }
}
