package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.CustomerRecurringTracker;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.RecurringModeEnum;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.repositories.CustomerRecurringTrackerRepository;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CustomerRecurringTrackerService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private CustomerRecurringTrackerRepository customerRecurringTrackerRepository;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;

    public void addToTracker(String customerId, String hostelId, int billingDay) {

        Date today = new Date();

        CustomerRecurringTracker rt = new CustomerRecurringTracker();
        rt.setCreatedAt(today);
        rt.setMode(RecurringModeEnum.MANUAL.name());
        rt.setHostelId(hostelId);
        rt.setCustomerId(customerId);
        rt.setCreationDay(billingDay);
        rt.setCreationMonth(Utils.getCurrentMonth(today));
        rt.setCreationYear(Utils.getCurrentYear(today));
        rt.setCreatedBy(authentication.getName());

        rt = customerRecurringTrackerRepository.save(rt);

        Agent agent = agentService.findUserByUserId(authentication.getName());

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.GENERATE_CUSTOMER_RECURRING,
                String.valueOf(rt.getTrackerId()), null, rt);
    }
}
