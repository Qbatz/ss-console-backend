package com.smartstay.console.services;

import com.smartstay.console.dao.DemoRequestActivity;
import com.smartstay.console.ennum.DemoRequestStatus;
import com.smartstay.console.repositories.DemoRequestActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class DemoRequestActivityService {

    @Autowired
    private DemoRequestActivityRepository demoRequestActivityRepository;

    public DemoRequestActivity save(DemoRequestActivity demoRequestActivity) {
        return demoRequestActivityRepository.save(demoRequestActivity);
    }

    public long getContactedCount(Date startDate, Date endDate) {
        String status = DemoRequestStatus.CONTACTED.name();
        return demoRequestActivityRepository.getStatusCount(startDate, endDate, status);
    }

    public long getDemoScheduledCount(Date startDate, Date endDate) {
        String status = DemoRequestStatus.DEMO_SCHEDULED.name();
        return demoRequestActivityRepository.getStatusCount(startDate, endDate, status);
    }

    public List<DemoRequestActivity> getDemoRequestActivitiesByRequestIds(Set<Long> demoRequestIds) {
        return demoRequestActivityRepository.findAllByRequestIdInOrderByActivityIdDesc(demoRequestIds);
    }

    public List<DemoRequestActivity> getDemoRequestActivitiesByRequestId(Long requestId) {
        return demoRequestActivityRepository.findAllByRequestIdOrderByActivityIdDesc(requestId);
    }

    public void deleteAll(List<DemoRequestActivity> demoRequestActivities) {
        demoRequestActivityRepository.deleteAll(demoRequestActivities);
    }
}
