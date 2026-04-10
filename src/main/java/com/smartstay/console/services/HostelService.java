package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.BillingRules;
import com.smartstay.console.dao.ElectricityConfig;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.repositories.HostelV1Repositories;
import com.smartstay.console.utils.Utils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class HostelService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private HostelV1Repositories hostelRepository;
    @Autowired
    private BillingRulesService billingRulesService;

    public HostelV1 getHostelInfo(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        try {
            return hostelRepository.getReferenceById(hostelId);
        }
        catch (EntityNotFoundException ene) {
            return null;
        }
    }

    public void updateHostel(HostelV1 hostelV1) {
        hostelRepository.save(hostelV1);
    }

    public List<HostelV1> getHostelsByHostelIds(Set<String> hostelIds) {
        return hostelRepository.findAllByHostelIdIn(hostelIds);
    }

    public List<HostelV1> getHostelsByHostelName(String hostelName) {
        return hostelRepository.findByHostelNameContainingIgnoreCase(hostelName);
    }

    public BillingDates getCurrentBillStartAndEndDates(String hostelId) {

        BillingRules billingRules = billingRulesService
                .getCurrentMonthTemplate(hostelId);

        int billStartDate = 1;
        int billingRuleDueDate = 5;
        if (billingRules != null) {
            billStartDate = billingRules.getBillingStartDate();
            billingRuleDueDate = billingRules.getBillDueDays();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, billStartDate);

        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.DAY_OF_MONTH) < billStartDate) {
            calendar.add(Calendar.MONTH, -1);
        }

//        Calendar calendarDueDate = Calendar.getInstance();
//        calendarDueDate.set(Calendar.DAY_OF_MONTH, billingRuleDate);

        Date dueDate = Utils.addDaysToDate(calendar.getTime(), billingRuleDueDate - 1);

        Date findEndDate = Utils.findLastDate(billStartDate, calendar.getTime());

        return new BillingDates(calendar.getTime(), findEndDate, dueDate, billingRuleDueDate,
                billingRules.isHasGracePeriod(), billingRules.getGracePeriodDays(),
                billingRules.getTypeOfBilling(), billingRules.getBillingModel());
    }

    public ElectricityConfig getElectricityConfig(String hostelId) {
        HostelV1 hostelV1 = hostelRepository.findById(hostelId).orElse(null);
        if (hostelV1 == null) {
            return null;
        }
        return hostelV1.getElectricityConfig();
    }

    public BillingDates getBillingRuleOnDate(String hostelId, Date date) {
        return billingRulesService.getBillingRuleByDateAndHostelId(hostelId, date);
    }

    public BillingDates getJoiningBasedCurrentMonthBillingDate(Date joiningDate, String hostelId, Date requestedDate) {
        BillingRules billingRules = billingRulesService.getCurrentMonthTemplate(hostelId);
        int billStartDate = 1;
        boolean hasGracePeriod = false;
        int billingRuleDueDate = 5;
        int gracePeriodDays = 0;
        if (billingRules != null) {
            billStartDate = billingRules.getBillingStartDate();
            billingRuleDueDate = billingRules.getBillDueDays();
            hasGracePeriod = billingRules.isHasGracePeriod();
            gracePeriodDays = billingRules.getGracePeriodDays();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(requestedDate);
        int day = Math.min(
                Utils.getDayOfMonth(joiningDate),
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        );
        calendar.set(Calendar.DAY_OF_MONTH, day);
        if (Utils.compareWithTwoDates(requestedDate, calendar.getTime()) < 0) {
            calendar.add(Calendar.MONTH, -1);
        }

        Date dueDate = Utils.addDaysToDate(calendar.getTime(), billingRuleDueDate - 1);
        Date endDate = Utils.findLastDate(Utils.getDayOfMonth(calendar.getTime()), calendar.getTime());

        return new BillingDates(calendar.getTime(),
                endDate,
                dueDate,
                billingRuleDueDate,
                hasGracePeriod,
                gracePeriodDays,
                billingRules.getTypeOfBilling(),
                billingRules.getBillingModel());
    }
}
