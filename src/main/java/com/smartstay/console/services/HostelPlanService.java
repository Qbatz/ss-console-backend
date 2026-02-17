package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.repositories.HostelPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HostelPlanService {
    @Autowired
    private Authentication authentication;

    @Autowired
    private HostelPlanRepository hostelPlanRepository;

    public List<HostelPlan> findAllHostelPlans(int size, int offset) {
        return hostelPlanRepository.findAllHostelPlans(size, offset);
    }

    public List<HostelPlan> findActiveHostels() {
        return hostelPlanRepository.findActiveHostels(new Date());
    }

    public List<HostelPlan> getHostelPlansByHostels(List<HostelV1> hostels) {
        return hostelPlanRepository.findAllByHostelIn(hostels);
    }
}
