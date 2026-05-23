package com.smartstay.console.services;

import com.smartstay.console.dao.DemoRequestActivity;
import com.smartstay.console.ennum.RequestStatus;
import com.smartstay.console.repositories.DemoRequestActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DemoRequestActivityService {

    @Autowired
    private DemoRequestActivityRepository demoRequestActivityRepository;

    public DemoRequestActivity save(DemoRequestActivity demoRequestActivity) {
        return demoRequestActivityRepository.save(demoRequestActivity);
    }

    public long getContactedCount(Date startDate, Date endDate) {
        String status = RequestStatus.CONTACTED.name();
        return demoRequestActivityRepository.getStatusCount(startDate, endDate, status);
    }

    public long getDemoScheduledCount(Date startDate, Date endDate) {
        String status = RequestStatus.DEMO_SCHEDULED.name();
        return demoRequestActivityRepository.getStatusCount(startDate, endDate, status);
    }
}
