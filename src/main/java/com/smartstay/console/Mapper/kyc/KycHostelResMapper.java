package com.smartstay.console.Mapper.kyc;

import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.KYCUsage;
import com.smartstay.console.responses.kyc.KycHostelRes;
import com.smartstay.console.utils.Utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KycHostelResMapper implements Function<HostelV1, KycHostelRes> {

    Map<String, List<Customers>> tenantHostelMap;
    Map<String, List<KYCUsage>> kycUsageHostelMap;

    public KycHostelResMapper(Map<String, List<Customers>> tenantHostelMap,
                              Map<String, List<KYCUsage>> kycUsageHostelMap) {
        this.tenantHostelMap = tenantHostelMap;
        this.kycUsageHostelMap = kycUsageHostelMap;
    }

    @Override
    public KycHostelRes apply(HostelV1 hostel) {

        String hostelId = hostel.getHostelId();

        String initials = null;
        if (hostel.getHostelName() != null) {
            initials = Utils.getInitials(hostel.getHostelName());
        }

        String fullAddress = Utils.buildFullAddress(hostel);

        boolean kycEnableStatus = false;

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

            tenantMap = tenants.stream()
                    .collect(Collectors.toMap(Customers::getCustomerId,
                            Function.identity(), (a, b) -> a));
        }

        if (kycUsageHostelMap != null){
            List<KYCUsage> kycUsages = kycUsageHostelMap.getOrDefault(hostelId, Collections.emptyList());

            for (KYCUsage kycUsage : kycUsages) {
                if (kycUsage.getLatestVerified() != null){
                    totalVerifiedTenant++;
                }

                int requestCount = kycUsage.getRequestCount() != null ? kycUsage.getRequestCount() : 0;
                int verifiedCount = kycUsage.getVerifiedCount() != null ? kycUsage.getVerifiedCount() : 0;

                totalRequests += requestCount;
                totalCompleted += verifiedCount;
            }

            Date lastRequestDate = null;
            Date lastCompletionDate = null;

            KYCUsage latestRequestByKycUsage = kycUsages.stream()
                    .filter(k -> k.getLatestRequest() != null)
                    .max(Comparator.comparing(KYCUsage::getLatestRequest))
                    .orElse(null);

            if (latestRequestByKycUsage != null){
                String latestRequestToId = latestRequestByKycUsage.getLatestRequestTo();
                if (latestRequestToId != null){
                    Customers latestRequestToTenant = tenantMap.getOrDefault(latestRequestToId, null);
                    if (latestRequestToTenant != null){
                        latestRequestTo = Utils.getFullName(latestRequestToTenant.getFirstName(), latestRequestToTenant.getLastName());
                    }
                }

                if (latestRequestByKycUsage.getLatestRequest() != null){
                    latestRequestDate = Utils.dateToString(latestRequestByKycUsage.getLatestRequest());
                    latestRequestTime = Utils.dateToTime(latestRequestByKycUsage.getLatestRequest());

                    lastRequestDate = latestRequestByKycUsage.getLatestRequest();
                }
            }

            KYCUsage latestCompletionByKycUsage = kycUsages.stream()
                    .filter(k -> k.getLatestVerified() != null)
                    .max(Comparator.comparing(KYCUsage::getLatestVerified))
                    .orElse(null);

            if (latestCompletionByKycUsage != null){
                String latestCompletionById = latestCompletionByKycUsage.getLatestCompletionBy();
                if (latestCompletionById != null){
                    Customers latestCompletionByTenant = tenantMap.getOrDefault(latestCompletionById, null);
                    if (latestCompletionByTenant != null){
                        latestCompletionBy = Utils.getFullName(latestCompletionByTenant.getFirstName(), latestCompletionByTenant.getLastName());
                    }
                }

                if (latestCompletionByKycUsage.getLatestVerified() != null){
                    latestCompletionDate = Utils.dateToString(latestCompletionByKycUsage.getLatestVerified());
                    latestCompletionTime = Utils.dateToTime(latestCompletionByKycUsage.getLatestVerified());

                    lastCompletionDate = latestCompletionByKycUsage.getLatestVerified();
                }
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

        return new KycHostelRes(hostelId, hostel.getHostelName(), initials, hostel.getMainImage(),
                hostel.getMobile(), hostel.getEmailId(), fullAddress, totalTenants, totalVerifiedTenant, latestRequestTo,
                latestCompletionBy, totalRequests, totalCompleted, kycEnableStatus, latestRequestDate,
                latestRequestTime, latestCompletionDate, latestCompletionTime, lastUpdatedDate, lastUpdatedTime);
    }
}
