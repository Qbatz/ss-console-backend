package com.smartstay.console.services;

import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.repositories.HostelPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HostelPlanService {

    @Autowired
    private HostelPlanRepository hostelPlanRepository;

    public Long findActiveHostels() {
        return hostelPlanRepository.findActiveHostels(new Date());
    }

    public List<HostelPlan> findByHostelIds(List<String> hostelIds) {
        return hostelPlanRepository.findByHostel_HostelIdIn(hostelIds);
    }

    public void saveAll(List<HostelPlan> listNewPlans) {
        hostelPlanRepository.saveAll(listNewPlans);
    }
}
