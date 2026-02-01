package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Plans;
import com.smartstay.console.ennum.PlanType;
import com.smartstay.console.repositories.PlansRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlansService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private PlansRepository plansRepository;

    public Plans findTrialPlan(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        return plansRepository.findPlanByPlanType(PlanType.TRIAL.name());
    }

    public Plans findPlanByPlanCode(String planCode) {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        return plansRepository.findByPlanCode(planCode);
    }
}
