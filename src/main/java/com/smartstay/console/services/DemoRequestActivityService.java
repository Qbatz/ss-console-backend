package com.smartstay.console.services;

import com.smartstay.console.dao.DemoRequestActivity;
import com.smartstay.console.repositories.DemoRequestActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class DemoRequestActivityService {

    @Autowired
    private DemoRequestActivityRepository demoRequestActivityRepository;

    public DemoRequestActivity save(DemoRequestActivity demoRequestActivity) {
        return demoRequestActivityRepository.save(demoRequestActivity);
    }

    public List<DemoRequestActivity> getDemoRequestActivitiesByRequestIds(Set<Long> demoRequestIds) {
        return demoRequestActivityRepository.findAllByRequestIdIn(demoRequestIds);
    }

    public List<DemoRequestActivity> getDemoRequestActivitiesByRequestId(Long requestId) {
        return demoRequestActivityRepository.findAllByRequestId(requestId);
    }
}
