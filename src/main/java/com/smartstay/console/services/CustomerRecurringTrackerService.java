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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

    public List<CustomerRecurringTracker> getLatestTrackersByCustomerIds(Set<String> customerIds){
        return customerRecurringTrackerRepository
                .findLatestRecurringTrackerByCustomerIds(customerIds);
    }

    public CustomerRecurringTracker getLatestTrackersByCustomerId(String customerId){
        return customerRecurringTrackerRepository
                .findTopByCustomerIdOrderByTrackerIdDesc(customerId);
    }

    public boolean checkRecurringTrackerExists(String customerId, int day, Date date, boolean isPostpaid) {

        int month;
        int year;

        if (isPostpaid){
            YearMonth previousYearMonth = Utils.getPreviousYearMonth(date);

            month = previousYearMonth.getMonthValue();
            year  = previousYearMonth.getYear();
        } else {
            month = Utils.getCurrentMonth(date);
            year =  Utils.getCurrentYear(date);
        }

        return customerRecurringTrackerRepository
                .existsByCustomerIdAndCreationDayAndCreationMonthAndCreationYear(customerId, day, month, year);
    }

    public Page<CustomerRecurringTracker> getPaginatedRecurringTrackersByCustomerId(String customerId, Pageable pageable) {
        return customerRecurringTrackerRepository
                .findAllByCustomerIdOrderByTrackerIdDesc(customerId, pageable);
    }
}
