package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.RecurringTracker;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.RecurringModeEnum;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.repositories.RecurringTrackerRepository;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class RecurringTrackerService {

    @Autowired
    private RecurringTrackerRepository recurringTrackerRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;

    public List<RecurringTracker> getLatestRecurringTrackersByHostelIds(Set<String> hostelIds) {
        return recurringTrackerRepository.getLatestRecurringTrackersByHostelIds(hostelIds);
    }

    public RecurringTracker getLatestRecurringTrackerByHostelId(String hostelId) {
        return recurringTrackerRepository.getLatestRecurringTrackerByHostelId(hostelId);
    }

    public void markAsInvoiceGenerated(String hostelId) {

        Calendar calendar = Calendar.getInstance();

        int billingDay = Utils.findDateFromDate(calendar.getTime());
        RecurringTracker rt = new RecurringTracker();
        rt.setCreatedAt(new Date());
        rt.setMode(RecurringModeEnum.MANUAL.name());
        rt.setHostelId(hostelId);
        rt.setCreationDay(billingDay);
        rt.setCreatedBy(authentication.getName());

        rt = recurringTrackerRepository.save(rt);

        Agent agent = agentService.findUserByUserId(authentication.getName());

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.GENERATE_RECURRING,
                String.valueOf(rt.getTrackerId()), null, rt);
    }
}
