package com.smartstay.console.services;

import com.smartstay.console.dao.ComplaintTypeV1;
import com.smartstay.console.repositories.ComplaintTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplaintTypeService {

    @Autowired
    private ComplaintTypeRepository complaintTypeRepository;

    public void deleteAll(List<ComplaintTypeV1> listComplaintTypes) {
        complaintTypeRepository.deleteAll(listComplaintTypes);
    }

    public List<ComplaintTypeV1> findByHostelId(String hostelId) {
        return complaintTypeRepository.findAllByHostelId(hostelId);
    }
}
