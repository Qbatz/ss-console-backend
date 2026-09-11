package com.smartstay.console.scheduler;

import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.KycConfig;
import com.smartstay.console.dao.Plans;
import com.smartstay.console.dto.hostelPlans.HostelPlan;
import com.smartstay.console.services.HostelPlanService;
import com.smartstay.console.services.KycConfigService;
import com.smartstay.console.services.PlansService;
import com.smartstay.console.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ActivatePlanScheduler {

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private HostelPlanService hostelPlanService;
    @Autowired
    private KycConfigService kycConfigService;
    @Autowired
    private PlansService plansService;

    @Scheduled(cron = "0 30 0 * * *")
    public void activatePlan() {

        List<HostelPlan> hostelPlanDtoList = subscriptionService.getHostelsActivatingToday();

        Date today = new Date();

        if (!hostelPlanDtoList.isEmpty()) {

            List<String> hostelIds = hostelPlanDtoList.stream()
                    .map(HostelPlan::hostelId)
                    .toList();

            List<com.smartstay.console.dao.HostelPlan> listHostelPlans = hostelPlanService
                    .findByHostelIds(hostelIds);
            if (listHostelPlans == null){
                listHostelPlans = new ArrayList<>();
            }

            List<KycConfig> kycConfigs = kycConfigService
                    .getAllByHostelIds(new HashSet<>(hostelIds));
            Map<String, KycConfig> kycConfigMap = kycConfigs.stream()
                    .collect(Collectors.toMap(KycConfig::getHostelId, kyc -> kyc,
                            (a, b) -> a));

            Set<String> hostelPlanCodes = listHostelPlans.stream()
                    .map(com.smartstay.console.dao.HostelPlan::getCurrentPlanCode)
                    .collect(Collectors.toSet());

            List<Plans> plans = plansService.getAllPlansByPlanCodes(hostelPlanCodes);
            Map<String, Plans> plansMap = plans.stream()
                    .collect(Collectors.toMap(Plans::getPlanCode, plan -> plan,
                            (a, b) -> a));

            if (!listHostelPlans.isEmpty()) {

                List<com.smartstay.console.dao.HostelPlan> listNewPlans = new ArrayList<>();
                List<KycConfig> savableKycConfigs = new ArrayList<>();

                for (com.smartstay.console.dao.HostelPlan hostelPlan : listHostelPlans){

                    if (hostelPlan == null || hostelPlan.getHostel() == null
                            || hostelPlan.getHostel().getHostelId() == null){
                        continue;
                    }

                    HostelV1 hostel = hostelPlan.getHostel();
                    String hostelId = hostel.getHostelId();

                    HostelPlan hostelPlanDto = hostelPlanDtoList
                            .stream()
                            .filter(i2 -> i2.hostelId().equalsIgnoreCase(hostelId))
                            .findFirst()
                            .orElse(null);

                    if (hostelPlanDto != null) {
                        hostelPlan.setCurrentPlanCode(hostelPlanDto.planCode());
                        hostelPlan.setCurrentPlanName(hostelPlanDto.planName());
                        hostelPlan.setCurrentPlanStartsAt(hostelPlanDto.startDate());
                        hostelPlan.setCurrentPlanEndsAt(hostelPlanDto.endDate());
                        hostelPlan.setCurrentPlanPrice(hostelPlanDto.planPrice());
                        hostelPlan.setPaidAmount(hostelPlanDto.paidAmount());
                        hostelPlan.setTrial(hostelPlanDto.isTrial());
                        hostelPlan.setTrialEndingAt(hostelPlanDto.trialEndingAt());

                        KycConfig kycConfig = kycConfigMap.getOrDefault(hostelId, null);
                        Plans plan = plansMap.getOrDefault(hostelPlanDto.planCode(), null);

                        int kycPerMonthLimit = -1;
                        if (plan != null){
                            kycPerMonthLimit = plan.getKycPerMonthLimit();
                        }

                        if (kycConfig == null){
                            kycConfig = new KycConfig();

                            kycConfig.setHostelId(hostelId);
                            kycConfig.setCanRequest(true);
                            kycConfig.setCreatedAt(today);
                        } else {
                            kycConfig.setUpdatedAt(today);
                        }

                        kycConfig.setLimitPerMonth(kycPerMonthLimit);

                        savableKycConfigs.add(kycConfig);
                    }

                    listNewPlans.add(hostelPlan);
                }

                hostelPlanService.saveAll(listNewPlans);
                kycConfigService.saveAll(savableKycConfigs);
            }
        }
    }
}
