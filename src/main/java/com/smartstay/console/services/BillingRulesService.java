package com.smartstay.console.services;

import com.smartstay.console.dao.BillingRules;
import com.smartstay.console.dto.billing.BillingPeriod;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.ennum.BillingModel;
import com.smartstay.console.ennum.BillingType;
import com.smartstay.console.exceptions.BadRequestException;
import com.smartstay.console.repositories.BillingRuleRepository;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public List<BillingRules> getLatestBillingRulesByHostelIdsAndBillingType(Set<String> hostelIds, String billingType) {
        return billingRuleRepository.findLatestBillingRulesByHostelIdsAndBillingType(hostelIds, billingType);
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

        Date dueDate = Utils.addDaysToDate(calendar.getTime(), billingRuleDueDate - 1);

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

    public BillingDates computeBillingDates(BillingRules billingRules, Date requestedDate) {

        if (requestedDate == null){
            throw new BadRequestException("Date is null");
        }

        requestedDate = Utils.getStartOfDay(requestedDate);

        int billStartDate = billingRules != null ? billingRules.getBillingStartDate() : 1;
        int billingRuleDueDate = billingRules != null ? billingRules.getBillDueDays() : 10;

        boolean hasGracePeriod = false;
        Integer gracePeriodDays = 0;
        String typeOfBilling = null;
        String billingModel = null;
        if (billingRules != null){
            hasGracePeriod = billingRules.isHasGracePeriod();
            gracePeriodDays = billingRules.getGracePeriodDays() != null ? billingRules.getGracePeriodDays() : 0;
            typeOfBilling = billingRules.getTypeOfBilling();
            billingModel = billingRules.getBillingModel();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(requestedDate);

        calendar.set(Calendar.DAY_OF_MONTH, billStartDate);

        if (Utils.compareWithTwoDates(requestedDate, calendar.getTime()) < 0) {
            calendar.add(Calendar.MONTH, -1);
        }

        Date dueDate = Utils.addDaysToDate(calendar.getTime(), billingRuleDueDate - 1);
        Date endDate = Utils.findLastDate(billStartDate, calendar.getTime());

        return new BillingDates(
                calendar.getTime(),
                endDate,
                dueDate,
                billingRuleDueDate,
                hasGracePeriod,
                gracePeriodDays,
                typeOfBilling,
                billingModel
        );
    }

    public BillingDates computeJoiningBasedBillingDates(BillingRules billingRules, Date joiningDate, Date requestedDate) {

        if (joiningDate == null || requestedDate == null){
            throw new BadRequestException("Date is null");
        }

        joiningDate = Utils.getStartOfDay(joiningDate);
        requestedDate = Utils.getStartOfDay(requestedDate);

        int billStartDate = 1;
        boolean hasGracePeriod = false;
        int billingRuleDueDate = 5;
        int gracePeriodDays = 0;
        String typeOfBilling = null;
        String billingModel = null;
        if (billingRules != null) {
            billStartDate = billingRules.getBillingStartDate();
            billingRuleDueDate = billingRules.getBillDueDays();
            hasGracePeriod = billingRules.isHasGracePeriod();
            gracePeriodDays = billingRules.getGracePeriodDays() != null ? billingRules.getGracePeriodDays() : 0;
            typeOfBilling = billingRules.getTypeOfBilling();
            billingModel = billingRules.getBillingModel();
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
                typeOfBilling,
                billingModel);
    }

    public BillingDates computeBillingDatesWithBillingModel(BillingRules billingRules, Date requestedDate) {

        if (billingRules == null || requestedDate == null){
            throw new BadRequestException("Date or billing rules is null");
        }

        BillingDates billingDates = null;

        if (BillingModel.PREPAID.name().equals(billingRules.getBillingModel())) {
            billingDates = computeBillingDates(billingRules, requestedDate);
        }
        else if (BillingModel.POSTPAID.name().equals(billingRules.getBillingModel())) {
            Date previousMonthDate = Utils.getPreviousMonthDate(requestedDate);
            billingDates = computeBillingDates(billingRules, previousMonthDate);
        }

        return billingDates;
    }

    public BillingDates computeJoiningBillingDatesWithBillingModel(BillingRules billingRules, Date joiningDate,
                                                                   Date requestedDate) {

        if (billingRules == null || joiningDate == null || requestedDate == null){
            throw new BadRequestException("Date or billing rules is null");
        }

        BillingDates billingDates = null;

        if (BillingModel.PREPAID.name().equals(billingRules.getBillingModel())) {
            billingDates = computeJoiningBasedBillingDates(billingRules, joiningDate, requestedDate);
        }
        else if (BillingModel.POSTPAID.name().equals(billingRules.getBillingModel())) {
            Date previousMonthDate = Utils.getPreviousMonthDate(requestedDate);
            billingDates = computeJoiningBasedBillingDates(billingRules, joiningDate, previousMonthDate);
        }

        return billingDates;
    }

    public BillingRules save(BillingRules billingRules) {
        return billingRuleRepository.save(billingRules);
    }

    public List<BillingDates> getBillingPeriods(BillingRules billingRules, Date joiningDate, Date today) {

        if (billingRules == null || joiningDate == null || today == null){
            throw new BadRequestException("Date or billing rules is null");
        }

        joiningDate = Utils.getStartOfDay(joiningDate);
        today = Utils.getStartOfDay(today);

        Map<BillingPeriod, BillingDates> periods = new LinkedHashMap<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(joiningDate);

        while (!calendar.getTime().after(today)) {

            Date referenceDate = calendar.getTime();

            BillingDates billingDates;

            if (BillingType.FIXED_DATE.name().equals(billingRules.getTypeOfBilling())) {

                billingDates = computeBillingDates(billingRules, referenceDate);

            } else if (BillingType.JOINING_DATE_BASED.name().equals(billingRules.getTypeOfBilling())) {

                billingDates = computeJoiningBasedBillingDates(billingRules, joiningDate, referenceDate);

            } else {
                throw new IllegalArgumentException(
                        "Unsupported billing type : " + billingRules.getTypeOfBilling());
            }

            BillingPeriod key = new BillingPeriod(
                    Utils.getStartOfDay(billingDates.currentBillStartDate()),
                    Utils.getStartOfDay(billingDates.currentBillEndDate())
            );

            periods.putIfAbsent(key, billingDates);

            calendar.add(Calendar.MONTH, 1);
        }

        return new ArrayList<>(periods.values());
    }

    public List<BillingDates> getExpectedBillingPeriodsForJoiningDateChange(BillingRules billingRules,
                                                                            Date joiningDate, Date today) {

        if (billingRules == null || joiningDate == null || today == null){
            throw new BadRequestException("Date or billing rules is null");
        }

        joiningDate = Utils.getStartOfDay(joiningDate);

        List<BillingDates> billingPeriods = getBillingPeriods(billingRules, joiningDate, today);

        Date finalJoiningDate = joiningDate;
        return billingPeriods.stream()
                .map(billingDates -> {

                    Date billStart = Utils.getStartOfDay(billingDates.currentBillStartDate());
                    Date billEnd = Utils.getStartOfDay(billingDates.currentBillEndDate());

                    /*
                     * If joining date falls after the billing period start,
                     * invoice should start from joining date.
                     */
                    if (finalJoiningDate.after(billStart)
                            && !finalJoiningDate.after(billEnd)) {

                        return new BillingDates(
                                finalJoiningDate,
                                billEnd,
                                billingDates.dueDate(),
                                billingDates.dueDays(),
                                billingDates.hasGracePeriod(),
                                billingDates.gracePeriodDays(),
                                billingDates.typeOfBilling(),
                                billingDates.billingModel()
                        );
                    }

                    return billingDates;
                }).toList();
    }
}
