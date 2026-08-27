package com.smartstay.console.Mapper.kyc;

import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.KycStatus;
import com.smartstay.console.responses.kyc.KycTenantRes;
import com.smartstay.console.responses.kyc.TenantKycRes;
import com.smartstay.console.services.BillingRulesService;
import com.smartstay.console.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class KycTenantResMapper implements Function<HostelV1, KycTenantRes> {

    List<Customers> tenants;
    BillingRules billingRule;
    BillingRulesService billingRulesService;
    List<Customers> allTenants;

    public KycTenantResMapper(List<Customers> tenants,
                              BillingRules billingRule,
                              BillingRulesService billingRulesService,
                              List<Customers> allTenants) {
        this.tenants = tenants;
        this.billingRule = billingRule;
        this.billingRulesService = billingRulesService;
        this.allTenants = allTenants;
    }

    @Override
    public KycTenantRes apply(HostelV1 hostel) {

        String hostelId = hostel.getHostelId();

        String initials = null;
        if (hostel.getHostelName() != null) {
            initials = Utils.getInitials(hostel.getHostelName());
        }

        String fullAddress = Utils.buildFullAddress(hostel);

        boolean kycEnableStatus = false;

        TenantKycResMapper tenantKycResMapper = new TenantKycResMapper(billingRule, billingRulesService);

        long totalTenants = 0;
        long totalRequested = 0;
        long totalVerified = 0;
        long totalWaitingForApproval = 0;
        if (allTenants != null) {
            totalTenants = allTenants.size();

            for (Customers tenant : allTenants) {

                KycDetails kycDetails =  tenant.getKycDetails();

                if (kycDetails != null) {
                    if (KycStatus.REQUESTED.name().equals(kycDetails.getCurrentStatus())) {
                        totalRequested++;
                    } else if (KycStatus.VERIFIED.name().equals(kycDetails.getCurrentStatus())) {
                        totalVerified++;
                    } else if (KycStatus.WAITING_FOR_APPROVAL.name().equals(kycDetails.getCurrentStatus())) {
                        totalWaitingForApproval++;
                    }
                }
            }
        }

        List<TenantKycRes> tenantsRes = new ArrayList<>();
        if (tenants != null){
            tenantsRes = tenants.stream()
                    .map(tenantKycResMapper)
                    .toList();
        }

        return new KycTenantRes(hostelId, hostel.getHostelName(), initials, hostel.getMainImage(),
                hostel.getMobile(), hostel.getEmailId(), fullAddress, totalTenants, totalRequested,
                totalVerified, totalWaitingForApproval, kycEnableStatus, tenantsRes);
    }
}
