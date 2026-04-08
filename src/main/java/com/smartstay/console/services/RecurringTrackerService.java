package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.RecurringTracker;
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

    public boolean checkRecurringTrackerExists(String hostelId, int day, Date date, boolean isPostpaid){

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

        return recurringTrackerRepository
                .existsByHostelIdAndCreationDayAndCreationMonthAndCreationYear(hostelId, day, month, year);
    }

    public void markAsInvoiceGenerated(String hostelId, int billingDay) {

        Date today = new Date();

        RecurringTracker rt = new RecurringTracker();
        rt.setCreatedAt(today);
        rt.setMode(RecurringModeEnum.MANUAL.name());
        rt.setHostelId(hostelId);
        rt.setCreationDay(billingDay);
        rt.setCreationMonth(Utils.getCurrentMonth(today));
        rt.setCreationYear(Utils.getCurrentYear(today));
        rt.setCreatedBy(authentication.getName());

        rt = recurringTrackerRepository.save(rt);

        Agent agent = agentService.findUserByUserId(authentication.getName());

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.GENERATE_RECURRING,
                String.valueOf(rt.getTrackerId()), null, rt);
    }

    public void markAsPostpaidInvoiceGenerated(String hostelId, int billingDay) {

        Date today = new Date();

        YearMonth previousYearMonth = Utils.getPreviousYearMonth(today);

        int month = previousYearMonth.getMonthValue();
        int year  = previousYearMonth.getYear();

        RecurringTracker rt = new RecurringTracker();
        rt.setCreatedAt(today);
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

    public RecurringTracker getLatestRecurringTrackerByHostelId(String hostelId) {
        return recurringTrackerRepository.getLatestRecurringTrackerByHostelId(hostelId);
    }

    public List<InvoiceCountPerTracker> getGeneratedInvoiceCountPerTracker(Set<Long> trackerIds) {
        return recurringTrackerRepository.getGeneratedInvoiceCountPerTracker(trackerIds);
    }
}
