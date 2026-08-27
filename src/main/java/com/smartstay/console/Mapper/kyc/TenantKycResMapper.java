package com.smartstay.console.Mapper.kyc;

import com.smartstay.console.dao.BillingRules;
import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.KycDetails;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.ennum.BillingType;
import com.smartstay.console.ennum.KycStatus;
import com.smartstay.console.responses.kyc.TenantKycRes;
import com.smartstay.console.services.BillingRulesService;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.function.Function;

public class TenantKycResMapper implements Function<Customers, TenantKycRes> {

    BillingRules billingRule;
    BillingRulesService billingRulesService;

    public TenantKycResMapper(BillingRules billingRule,
                              BillingRulesService billingRulesService) {
        this.billingRule = billingRule;
        this.billingRulesService = billingRulesService;
    }

    @Override
    public TenantKycRes apply(Customers customer) {

        String customerId = customer.getCustomerId();
        KycDetails kycDetails = customer.getKycDetails();

        Date today = new Date();

        String fullName = Utils.getFullName(customer.getFirstName(), customer.getLastName());

        String joiningDate = null;
        if (customer.getJoiningDate() != null) {
            joiningDate = Utils.dateToString(customer.getJoiningDate());
        }

        String billingCycleStart = null;
        String billingCycleEnd = null;
        if (billingRule != null) {
            if (BillingType.FIXED_DATE.name().equals(billingRule.getTypeOfBilling())){
                BillingDates billingDates = billingRulesService.computeBillingDates(billingRule, today);

                if (billingDates != null){
                    billingCycleStart = Utils.dateToMonthDate(billingDates.currentBillStartDate());
                    billingCycleEnd = Utils.dateToMonthDate(billingDates.currentBillEndDate());
                }
            } else if (BillingType.JOINING_DATE_BASED.name().equals(billingRule.getTypeOfBilling())) {
                BillingDates billingDates = billingRulesService
                        .computeJoiningBasedBillingDates(billingRule, customer.getJoiningDate(), today);

                if (billingDates != null){
                    billingCycleStart = Utils.dateToMonthDate(billingDates.currentBillStartDate());
                    billingCycleEnd = Utils.dateToMonthDate(billingDates.currentBillEndDate());
                }
            }
        }

        String kycCompletedDate = null;
        String kycCompletedTime = null;

        String kycDetailsStatus = null;

        boolean canSendReminder = false;
        boolean canApproveKyc = false;

        if (kycDetails != null) {
            kycDetailsStatus = kycDetails.getCurrentStatus();
            if (KycStatus.WAITING_FOR_APPROVAL.name().equals(kycDetailsStatus)) {
                canApproveKyc = true;
            }
            if (kycDetails.getCompletedAt() != null){
                kycCompletedDate = Utils.dateToString(kycDetails.getCompletedAt());
                kycCompletedTime = Utils.dateToTime(kycDetails.getCompletedAt());
            }
        }

        return new TenantKycRes(customerId, customer.getFirstName(), customer.getLastName(), fullName,
                joiningDate, billingCycleStart, billingCycleEnd, kycCompletedDate, kycCompletedTime,
                kycDetailsStatus, canSendReminder, canApproveKyc);
    }
}
