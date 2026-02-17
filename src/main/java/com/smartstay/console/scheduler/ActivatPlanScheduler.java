package com.smartstay.console.scheduler;

import com.smartstay.console.dao.Subscription;
import com.smartstay.console.dto.hostelPlans.HostelPlan;
import com.smartstay.console.services.HostelPlanService;
import com.smartstay.console.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActivatPlanScheduler {

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private HostelPlanService hostelPlanService;

    @Scheduled(cron = "0 30 0 * * *")
    public void activatePlan() {
        List<HostelPlan> hp = subscriptionService.getHostelsActivatingToday();
        if (!hp.isEmpty()) {
            List<String> hostelIds = hp.stream()
                    .map(HostelPlan::hostelId)
                    .toList();

            List<com.smartstay.console.dao.HostelPlan> listHostelPlans = hostelPlanService.findByHostelIds(hostelIds);
            if (listHostelPlans != null && !listHostelPlans.isEmpty()) {
                List<com.smartstay.console.dao.HostelPlan> listNewPlans = subscriptionPlanMapper(listHostelPlans, hp);
                hostelPlanService.saveAll(listNewPlans);
            }

        }
    }


    List<com.smartstay.console.dao.HostelPlan> subscriptionPlanMapper(List<com.smartstay.console.dao.HostelPlan> hp, List<HostelPlan> newScubscription) {
        List<com.smartstay.console.dao.HostelPlan> newPlan = hp
                .stream()
                .map(i -> {
                    HostelPlan hostelPlan = newScubscription
                            .stream()
                            .filter(i2 -> i2.hostelId().equalsIgnoreCase(i.getHostel().getHostelId()))
                            .findFirst()
                            .orElse(null);
                    if (hostelPlan != null) {
                        i.setCurrentPlanCode(hostelPlan.planCode());
                        i.setCurrentPlanName(hostelPlan.planName());
                        i.setCurrentPlanStartsAt(hostelPlan.startDate());
                        i.setCurrentPlanEndsAt(hostelPlan.endDate());
                    }
                    return  i;
                })
                .toList();

        return newPlan;
    }
}
