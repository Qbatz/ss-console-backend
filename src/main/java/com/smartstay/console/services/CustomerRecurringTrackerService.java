package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.CustomerRecurringTracker;
import com.smartstay.console.dto.hostel.BillingDates;
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

    public void addToTracker(String customerId, String hostelId, int billingDay, BillingDates billingDates) {

        Date date = new Date();

        if (billingDates != null){
            date = billingDates.currentBillStartDate();
        }

        int month = Utils.getCurrentMonth(date);
        int year = Utils.getCurrentYear(date);

        CustomerRecurringTracker rt = new CustomerRecurringTracker();
        rt.setCreatedAt(date);
        rt.setMode(RecurringModeEnum.MANUAL.name());
        rt.setHostelId(hostelId);
        rt.setCustomerId(customerId);
        rt.setCreationDay(billingDay);
        rt.setCreationMonth(month);
        rt.setCreationYear(year);
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

    public CustomerRecurringTracker getLatestTrackerByHostelId(String hostelId){
        return customerRecurringTrackerRepository
                .findTopByHostelIdOrderByTrackerIdDesc(hostelId);
    }

    public boolean checkRecurringTrackerExists(String customerId, int day, Date date) {

        int month;
        int year;

        month = Utils.getCurrentMonth(date);
        year =  Utils.getCurrentYear(date);

        return customerRecurringTrackerRepository
                .existsByCustomerIdAndCreationDayAndCreationMonthAndCreationYear(customerId, day, month, year);
    }

    public Page<CustomerRecurringTracker> getPaginatedRecurringTrackersByCustomerId(String customerId, Pageable pageable) {
        return customerRecurringTrackerRepository
                .findAllByCustomerIdOrderByTrackerIdDesc(customerId, pageable);
    }

    public List<CustomerRecurringTracker> getRecurringTrackersByHostelId(String hostelId) {
        return customerRecurringTrackerRepository
                .findAllByHostelIdOrderByTrackerIdDesc(hostelId);
    }

    public List<CustomerRecurringTracker> findByHostelIdAndCustomerId(String hostelId, String customerId) {
        return customerRecurringTrackerRepository.findAllByHostelIdAndCustomerId(hostelId, customerId);
    }

    public void deleteAll(List<CustomerRecurringTracker> listCustomerRecurringTrackers) {
        customerRecurringTrackerRepository.deleteAll(listCustomerRecurringTrackers);
    }

    public List<CustomerRecurringTracker> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return customerRecurringTrackerRepository.findAllByHostelIdAndCustomerIdIn(hostelId, customerIds);
    }
}
