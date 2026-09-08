package com.smartstay.console.Mapper.kyc;

import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.KycStatus;
import com.smartstay.console.responses.kyc.KycHostelRes;
import com.smartstay.console.utils.Utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KycHostelResMapper implements Function<HostelV1, KycHostelRes> {

    Map<String, List<Customers>> tenantHostelMap;
    Map<String, KYCUsage> kycUsageHostelMap;
    Map<String, KycConfig> kycConfigMap;
    Map<String, KycHistory> latestKycHistoryMap;

    public KycHostelResMapper(Map<String, List<Customers>> tenantHostelMap,
                              Map<String, KYCUsage> kycUsageHostelMap,
                              Map<String, KycConfig> kycConfigMap,
                              Map<String, KycHistory> latestKycHistoryMap) {
        this.tenantHostelMap = tenantHostelMap;
        this.kycUsageHostelMap = kycUsageHostelMap;
        this.kycConfigMap = kycConfigMap;
        this.latestKycHistoryMap = latestKycHistoryMap;
    }

    @Override
    public KycHostelRes apply(HostelV1 hostel) {

        String hostelId = hostel.getHostelId();

        String initials = null;
        if (hostel.getHostelName() != null) {
            initials = Utils.getInitials(hostel.getHostelName());
        }

        String fullAddress = Utils.buildFullAddress(hostel);

        Date today = new Date();
        Date todayEnd = Utils.getEndOfDay(today);

        boolean kycEnableStatus = false;
        if (latestKycHistoryMap != null) {
            KycHistory latestKycHistory = latestKycHistoryMap.getOrDefault(hostelId, null);
            if (latestKycHistory != null){
                if (latestKycHistory.getEndDate() != null){
                    Date endDateStart = Utils.getStartOfDay(latestKycHistory.getEndDate());
                    if (!endDateStart.before(todayEnd)){
                        kycEnableStatus = true;
                    }
                } else {
                    kycEnableStatus = true;
                }
            }
        }

        int kycLimitPerMonth = -1;
        if (kycConfigMap != null) {
            KycConfig kycConfig = kycConfigMap.getOrDefault(hostelId, null);
            if (kycConfig != null){
                if (kycConfig.getLimitPerMonth() != null){
                    kycLimitPerMonth = kycConfig.getLimitPerMonth();
                }
            }
        }

        Map<String, Customers> tenantMap = new HashMap<>();

        long totalTenants = 0;
        long totalVerifiedTenant = 0;
        String latestRequestTo = null;
        String latestCompletionBy = null;
        long totalRequests = 0;
        long totalCompleted = 0;
        String latestRequestDate = null;
        String latestRequestTime = null;
        String latestCompletionDate = null;
        String latestCompletionTime = null;
        String lastUpdatedDate = null;
        String lastUpdatedTime = null;

        if (tenantHostelMap != null) {
            List<Customers> tenants = tenantHostelMap.getOrDefault(hostelId, Collections.emptyList());

            totalTenants = tenants.size();

            for (Customers customer : tenants) {
                KycDetails kycDetails = customer.getKycDetails();

                if (kycDetails != null){
                    if (KycStatus.VERIFIED.name().equals(kycDetails.getCurrentStatus())){
                        totalVerifiedTenant++;
                    }
                }
            }

            tenantMap = tenants.stream()
                    .collect(Collectors.toMap(Customers::getCustomerId,
                            Function.identity(), (a, b) -> a));
        }

        if (kycUsageHostelMap != null){
            KYCUsage kycUsage = kycUsageHostelMap.getOrDefault(hostelId, null);

            if (kycUsage != null){

                int requestCount = kycUsage.getRequestCount() != null ? kycUsage.getRequestCount() : 0;
                int verifiedCount = kycUsage.getVerifiedCount() != null ? kycUsage.getVerifiedCount() : 0;

                totalRequests = requestCount;
                totalCompleted = verifiedCount;

                Date lastRequestDate = null;
                Date lastCompletionDate = null;

                String latestRequestToId = kycUsage.getLatestRequestTo();
                if (latestRequestToId != null){
                    Customers latestRequestToTenant = tenantMap.getOrDefault(latestRequestToId, null);
                    if (latestRequestToTenant != null){
                        latestRequestTo = Utils.getFullName(latestRequestToTenant.getFirstName(), latestRequestToTenant.getLastName());
                    }
                }

                if (kycUsage.getLatestRequest() != null){
                    latestRequestDate = Utils.dateToString(kycUsage.getLatestRequest());
                    latestRequestTime = Utils.dateToTime(kycUsage.getLatestRequest());

                    lastRequestDate = kycUsage.getLatestRequest();
                }

                String latestCompletionById = kycUsage.getLatestCompletionBy();
                if (latestCompletionById != null){
                    Customers latestCompletionByTenant = tenantMap.getOrDefault(latestCompletionById, null);
                    if (latestCompletionByTenant != null){
                        latestCompletionBy = Utils.getFullName(latestCompletionByTenant.getFirstName(), latestCompletionByTenant.getLastName());
                    }
                }

                if (kycUsage.getLatestVerified() != null){
                    latestCompletionDate = Utils.dateToString(kycUsage.getLatestVerified());
                    latestCompletionTime = Utils.dateToTime(kycUsage.getLatestVerified());

                    lastCompletionDate = kycUsage.getLatestVerified();
                }

                Date lastUpdated = Stream.of(lastRequestDate, lastCompletionDate)
                        .filter(Objects::nonNull)
                        .max(Date::compareTo)
                        .orElse(null);

                if (lastUpdated != null) {
                    lastUpdatedDate = Utils.dateToString(lastUpdated);
                    lastUpdatedTime = Utils.dateToTime(lastUpdated);
                }
            }
        }

        return new KycHostelRes(hostelId, hostel.getHostelName(), initials, hostel.getMainImage(),
                hostel.getMobile(), hostel.getEmailId(), fullAddress, totalTenants, totalVerifiedTenant, latestRequestTo,
                latestCompletionBy, totalRequests, totalCompleted, kycEnableStatus, kycLimitPerMonth, latestRequestDate,
                latestRequestTime, latestCompletionDate, latestCompletionTime, lastUpdatedDate, lastUpdatedTime);
    }
}
