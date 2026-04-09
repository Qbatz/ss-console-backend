package com.smartstay.console.services;

import com.smartstay.console.dao.BillingRules;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.ennum.BillingType;
import com.smartstay.console.repositories.BillingRuleRepository;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class BillingRulesService {

    @Autowired
    BillingRuleRepository billingRuleRepository;

    public Page<BillingRules> getPaginatedBillingRulesByDays(Set<Integer> days, String billingType,
                                                             Set<String> hostelIds, String billingModel,
                                                             Pageable pageable) {
        return billingRuleRepository.getPaginatedBillingRulesByDays(days, billingType, hostelIds, billingModel, pageable);
    }

    public BillingRules getCurrentMonthTemplate(String hostelId) {
        return billingRuleRepository.findCurrentBillingRules(hostelId);
    }

    public List<BillingRules> getLatestBillingRulesByHostelIds(Set<String> hostelIds) {
        return billingRuleRepository.findLatestBillingRulesByHostelIds(hostelIds);
    }

    public BillingDates getBillingRuleByDateAndHostelId(String hostelId, Date dateJoiningDate) {
        BillingRules billingRules = billingRuleRepository.findCurrentBillingRules(hostelId);
        BillingDates billDates = null;

        int billStartDate = 1;
        int billingRuleDueDate = 10;
        int billMonth;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateJoiningDate);

        if (billingRules != null) {
            billStartDate = billingRules.getBillingStartDate();
            billingRuleDueDate = billingRules.getBillDueDays();

            if (billingRules.getTypeOfBilling().equalsIgnoreCase(BillingType.JOINING_DATE_BASED.name())) {
                calendar.set(Calendar.DAY_OF_MONTH, billStartDate);
                Date findEndDate = Utils.findLastDate(billStartDate, calendar.getTime());

                return new BillingDates(calendar.getTime(),
                        findEndDate,
                        null,
                        billingRules.getBillDueDays(),
                        billingRules.isHasGracePeriod(),
                        billingRules.getGracePeriodDays(),
                        billingRules.getTypeOfBilling(),
                        billingRules.getBillingModel());
            }
        }

        calendar.set(Calendar.DAY_OF_MONTH, billStartDate);
        if (Utils.compareWithTwoDates(dateJoiningDate, calendar.getTime()) < 0) {
            calendar.add(Calendar.MONTH, -1);
        }

        Date dueDate = Utils.addDaysToDate(calendar.getTime(), billingRuleDueDate);

        Date findEndDate = Utils.findLastDate(billStartDate, calendar.getTime());

        if (billingRules != null) {
            billDates = new BillingDates(calendar.getTime(),
                    findEndDate,
                    dueDate,
                    billingRuleDueDate,
                    billingRules.isHasGracePeriod(),
                    billingRules.getGracePeriodDays(),
                    billingRules.getTypeOfBilling(),
                    billingRules.getBillingModel());
        }
        return billDates;
    }

    public List<BillingRules> getLatestBillingRulesByDays(Set<Integer> days, String billingType) {
        return billingRuleRepository.getLatestBillingRulesByDays(days, billingType);
    }
}
