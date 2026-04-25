package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.RecurringTracker;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.dto.hostel.InvoiceCountPerTracker;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.RecurringModeEnum;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.repositories.RecurringTrackerRepository;
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

    public boolean checkRecurringTrackerExists(String hostelId, int day, Date date){

        int month;
        int year;

        month = Utils.getCurrentMonth(date);
        year =  Utils.getCurrentYear(date);

        return recurringTrackerRepository
                .existsByHostelIdAndCreationDayAndCreationMonthAndCreationYear(hostelId, day, month, year);
    }

    public void markAsInvoiceGenerated(String hostelId, int billingDay, BillingDates billingDates) {

        Date date = new Date();

        if (billingDates != null){
            date = billingDates.currentBillStartDate();
        }

        int month = Utils.getCurrentMonth(date);
        int year = Utils.getCurrentYear(date);

        RecurringTracker rt = new RecurringTracker();
        rt.setCreatedAt(date);
        rt.setMode(RecurringModeEnum.MANUAL.name());
        rt.setHostelId(hostelId);
        rt.setCreationDay(billingDay);
        rt.setCreationMonth(month);
        rt.setCreationYear(year);
        rt.setCreatedBy(authentication.getName());

        rt = recurringTrackerRepository.save(rt);

        Agent agent = agentService.findUserByUserId(authentication.getName());

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.GENERATE_RECURRING,
                String.valueOf(rt.getTrackerId()), null, rt);
    }

    public void markAsPostpaidInvoiceGenerated(String hostelId, int billingDay, BillingDates billingDates) {

        Date date = new Date();

        if (billingDates != null){
            date = billingDates.currentBillStartDate();
        }

        int month = Utils.getCurrentMonth(date);
        int year = Utils.getCurrentYear(date);

        RecurringTracker rt = new RecurringTracker();
        rt.setCreatedAt(date);
        rt.setMode(RecurringModeEnum.MANUAL.name());
        rt.setHostelId(hostelId);
        rt.setCreationDay(billingDay);
        rt.setCreationMonth(month);
        rt.setCreationYear(year);
        rt.setCreatedBy(authentication.getName());

        rt = recurringTrackerRepository.save(rt);

        Agent agent = agentService.findUserByUserId(authentication.getName());

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.GENERATE_RECURRING,
                String.valueOf(rt.getTrackerId()), null, rt);
    }

    public Page<RecurringTracker> getPaginatedRecurringTrackersByHostelId(String hostelId, Pageable pageable) {
        return recurringTrackerRepository.findAllByHostelIdOrderByTrackerIdDesc(hostelId, pageable);
    }

    public List<RecurringTracker> getRecurringTrackersByHostelId(String hostelId) {
        return recurringTrackerRepository.findAllByHostelIdOrderByTrackerIdDesc(hostelId);
    }

    public RecurringTracker getLatestRecurringTrackerByHostelId(String hostelId) {
        return recurringTrackerRepository.getLatestRecurringTrackerByHostelId(hostelId);
    }

    public List<InvoiceCountPerTracker> getGeneratedInvoiceCountPerTracker(Set<Long> trackerIds) {
        return recurringTrackerRepository.getGeneratedInvoiceCountPerTracker(trackerIds);
    }

    public RecurringTracker getRecurringTrackerByDayMonthYear(String hostelId, int day, Date date) {

        int month = Utils.getCurrentMonth(date);
        int year =  Utils.getCurrentYear(date);

        return recurringTrackerRepository
                .findByHostelIdAndCreationDayAndCreationMonthAndCreationYear(hostelId, day, month, year);
    }

    public void delete(RecurringTracker recurringTracker) {
        recurringTrackerRepository.delete(recurringTracker);
    }

    public void save(RecurringTracker recurringTracker) {
        recurringTrackerRepository.save(recurringTracker);
    }
}
