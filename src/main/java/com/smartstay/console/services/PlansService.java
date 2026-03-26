package com.smartstay.console.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.console.Mapper.plans.PlanResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.PlanFeatures;
import com.smartstay.console.dao.Plans;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.PlanType;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.plans.PlanFeaturesUpdatePayload;
import com.smartstay.console.payloads.plans.PlansUpdatePayload;
import com.smartstay.console.repositories.PlansRepository;
import com.smartstay.console.responses.plans.PlansResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlansService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private PlansRepository plansRepository;
    @Autowired
    private AgentService  agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private PlanFeaturesService planFeaturesService;

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

    public ResponseEntity<?> getAllPlans() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Plans.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<Plans> plans = plansRepository.findActivePlansExcludingTrial();

        List<PlansResponse> responses = plans.stream()
                .map(plan -> new PlanResMapper().apply(plan))
                .toList();

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

//    public ResponseEntity<?> updatePlanByPlanId(Long planId, PlansUpdatePayload payload) {
//
//        if (!authentication.isAuthenticated()) {
//            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
//        }
//
//        Agent agent = agentService.findUserByUserId(authentication.getName());
//        if (agent == null) {
//            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
//        }
//
//        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Plans.getId(), Utils.PERMISSION_READ)) {
//            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
//        }
//
//        Plans plan = plansRepository.findByPlanIdAndIsActiveTrue(planId);
//        if (plan == null) {
//            return new ResponseEntity<>(Utils.PLAN_NOT_FOUND, HttpStatus.BAD_REQUEST);
//        }
//        Plans oldPlan = new ObjectMapper().convertValue(plan, Plans.class);
//
//        plan.setPlanName();
//        plan.setPlanCode();
//        plan.setPlanType();
//        plan.setDuration();
//        plan.setPrice();
//        plan.setDiscounts();
//        plan.setShouldShow();
//        plan.setCanCustomize();
//        plan.setUpdatedAt(new Date());
//
//        List<PlanFeatures> planFeatures = planFeaturesService.findAllByIds();
//        Map<Long, PlanFeatures> planFeaturesMap = planFeatures.stream()
//                .collect(Collectors.toMap(PlanFeatures::getId, p -> p));
//
//        for (PlanFeaturesUpdatePayload planFeaturesUpdatePayload : payload.planFeatures()){
//
//        }
//
//        plan = plansRepository.save(plan);
//
//        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.PLANS,
//                String.valueOf(planId), oldPlan, plan);
//
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
}
