package com.smartstay.console.services;

import com.smartstay.console.Mapper.plans.PlanResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.PlanFeatures;
import com.smartstay.console.dao.Plans;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.PlanType;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.plans.PlanFeaturesPayload;
import com.smartstay.console.payloads.plans.PlanFeaturesUpdatePayload;
import com.smartstay.console.payloads.plans.PlansPayload;
import com.smartstay.console.payloads.plans.PlansUpdatePayload;
import com.smartstay.console.repositories.PlansRepository;
import com.smartstay.console.responses.plans.PlansResponse;
import com.smartstay.console.utils.CloneUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
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

    private static final String ALPHABETS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    public Plans findTrialPlan() {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        return plansRepository.findTopByPlanTypeAndIsActiveTrueOrderByPlanIdAsc(PlanType.TRIAL.name());
    }

    public Plans findLatestTrialPlan() {
        return plansRepository.findTopByPlanTypeAndIsActiveTrueOrderByPlanIdDesc(PlanType.TRIAL.name());
    }

    public Plans findPlanByPlanCode(String planCode) {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        return plansRepository.findByPlanCodeAndIsActiveTrue(planCode);
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

    public ResponseEntity<?> updatePlanByPlanId(Long planId, PlansUpdatePayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Plans.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Plans plan = plansRepository.findByPlanIdAndIsActiveTrue(planId);
        if (plan == null) {
            return new ResponseEntity<>(Utils.PLAN_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Plans oldPlan = CloneUtility.clonePlans(plan);

        if (payload.planName() != null && !payload.planName().isBlank()){
            if (!payload.planName().equalsIgnoreCase(plan.getPlanName()) &&
                    plansRepository.existsByPlanNameIgnoreCaseAndPlanIdNot(payload.planName(), planId)){
                return new ResponseEntity<>(Utils.PLAN_NAME_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }
            plan.setPlanName(payload.planName());
        }
        if (payload.planCode() != null && !payload.planCode().isBlank()){
            if (!payload.planCode().equalsIgnoreCase(plan.getPlanCode()) &&
                    plansRepository.existsByPlanCodeIgnoreCaseAndPlanIdNot(payload.planCode(), planId)){
                return new ResponseEntity<>(Utils.PLAN_CODE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }
            plan.setPlanCode(payload.planCode());
        }
        if (payload.planType() != null && !payload.planType().isBlank()){
            if (!payload.planType().equalsIgnoreCase(plan.getPlanType()) &&
                    plansRepository.existsByPlanTypeIgnoreCaseAndPlanIdNot(payload.planType(), planId)){
                return new ResponseEntity<>(Utils.PLAN_TYPE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }
            plan.setPlanType(payload.planType());
        }
        if (payload.duration() != null){
            plan.setDuration(payload.duration());
        }
        if (payload.price() != null && payload.price() >= 0){
            plan.setPrice(payload.price());
        }
        if (payload.discountPercentage() != null
                && payload.discountPercentage() >= 0
                && payload.discountPercentage() <= 100){
            plan.setDiscounts(payload.discountPercentage());
        }
        if (payload.shouldShow() != null) {
            plan.setShouldShow(payload.shouldShow());
        }
        if (payload.canCustomize() != null) {
            plan.setCanCustomize(payload.canCustomize());
        }
        plan.setUpdatedAt(new Date());

        if (payload.planFeatures() != null && !payload.planFeatures().isEmpty()) {

            Set<Long> planFeatureIds = payload.planFeatures().stream()
                    .map(PlanFeaturesUpdatePayload::planFeatureId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<PlanFeatures> planFeatures = planFeaturesService.findAllByIds(planFeatureIds);

            Map<Long, PlanFeatures> planFeaturesMap = planFeatures.stream()
                    .collect(Collectors.toMap(PlanFeatures::getId, p -> p));

            List<PlanFeatures> updatedPlanFeatures = new ArrayList<>();

            for (PlanFeaturesUpdatePayload pfPayload : payload.planFeatures()) {
                PlanFeatures planFeature = planFeaturesMap.get(pfPayload.planFeatureId());

                if (planFeature != null) {
                    if (!planFeature.getPlan().equals(plan)) {
                        return new ResponseEntity<>(Utils.PLAN_FEATURE_MISMATCH, HttpStatus.BAD_REQUEST);
                    }
                    if (pfPayload.featureName() != null && !pfPayload.featureName().isBlank()) {
                        planFeature.setFeatureName(pfPayload.featureName());
                    }
                    if (pfPayload.price() != null){
                        planFeature.setPrice(pfPayload.price());
                    }
                    updatedPlanFeatures.add(planFeature);
                }
            }

            planFeaturesService.saveAll(updatedPlanFeatures);
        }

        plan = plansRepository.save(plan);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.PLANS,
                String.valueOf(planId), oldPlan, plan);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> addPlan(PlansPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Plans.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Plans plan = new Plans();

        if (plansRepository.existsByPlanNameIgnoreCase(payload.planName())){
            return new ResponseEntity<>(Utils.PLAN_NAME_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
        if (plansRepository.existsByPlanTypeIgnoreCase(payload.planType())){
            return new ResponseEntity<>(Utils.PLAN_TYPE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
        plan.setPlanName(payload.planName());
        plan.setPrice(payload.price());
        plan.setDuration(payload.duration());
        plan.setDiscounts(payload.discountPercentage());
        plan.setPlanType(payload.planType());

        String planCode = null;
        if (payload.planCode() != null && !payload.planCode().isBlank()) {
            if (plansRepository.existsByPlanCodeIgnoreCase(payload.planCode())){
                return new ResponseEntity<>(Utils.PLAN_CODE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }
            planCode = payload.planCode();
        } else {
            do {
                planCode = generatePlanCode();
            } while (plansRepository.existsByPlanCodeIgnoreCase(planCode));
        }
        plan.setPlanCode(planCode);

        plan.setShouldShow(payload.shouldShow());
        plan.setCanCustomize(payload.canCustomize());
        plan.setActive(true);
        plan.setCreatedAt(new Date());
        plan.setUpdatedAt(null);

        List<PlanFeatures> planFeatures = new ArrayList<>();
        if (payload.planFeatures() != null && !payload.planFeatures().isEmpty()) {
            for (PlanFeaturesPayload pfPayload : payload.planFeatures()) {

                if (pfPayload.featureName() != null && pfPayload.featureName().isBlank()) {
                    return new ResponseEntity<>(Utils.PLAN_FEATURE_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
                }
                PlanFeatures planFeature = new PlanFeatures();

                planFeature.setFeatureName(pfPayload.featureName());
                planFeature.setPrice(pfPayload.price() != null ? pfPayload.price() : 0);
                planFeature.setActive(true);
                planFeature.setPlan(plan);

                planFeatures.add(planFeature);
            }
        }

        plan.setFeaturesList(planFeatures);

        plan = plansRepository.save(plan);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.PLANS,
                String.valueOf(plan.getPlanId()), null, plan);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> deactivatePlan(Long planId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Plans.getId(), Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Plans plan = plansRepository.findByPlanIdAndIsActiveTrue(planId);
        if (plan == null) {
            return new ResponseEntity<>(Utils.PLAN_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Plans oldPlan = CloneUtility.clonePlans(plan);

        plan.setActive(false);
        plan.setUpdatedAt(new Date());

        for (PlanFeatures feature : plan.getFeaturesList()) {
            feature.setActive(false);
        }

        plansRepository.save(plan);

        agentActivitiesService.createAgentActivity(agent, ActivityType.DEACTIVATE, Source.PLANS,
                String.valueOf(planId), oldPlan, null);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> addPlanFeature(Long planId, PlanFeaturesPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Plans.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Plans plan = plansRepository.findByPlanIdAndIsActiveTrue(planId);
        if (plan == null) {
            return new ResponseEntity<>(Utils.PLAN_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        PlanFeatures planFeature = new PlanFeatures();

        planFeature.setFeatureName(payload.featureName());
        planFeature.setPrice(payload.price() != null ? payload.price() : 0);
        planFeature.setActive(true);
        planFeature.setPlan(plan);

        planFeaturesService.save(planFeature);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.PLAN_FEATURES,
                String.valueOf(planFeature.getId()), null, planFeature);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> deactivatePlanFeature(Long planFeatureId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Plans.getId(), Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        PlanFeatures planFeature = planFeaturesService.findById(planFeatureId);
        if (planFeature == null) {
            return new ResponseEntity<>(Utils.PLAN_FEATURE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        PlanFeatures oldPlanFeature = CloneUtility.clonePlanFeatures(planFeature);

        planFeature.setActive(false);

        planFeaturesService.save(planFeature);

        agentActivitiesService.createAgentActivity(agent, ActivityType.DEACTIVATE, Source.PLAN_FEATURES,
                String.valueOf(planFeatureId), oldPlanFeature, null);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public static String generatePlanCode() {
        StringBuilder ref = new StringBuilder();

        // 4 letters
        for (int i = 0; i < 2; i++) {
            ref.append(ALPHABETS.charAt(RANDOM.nextInt(ALPHABETS.length())));
        }

        // 4 digits
        for (int i = 0; i < 2; i++) {
            ref.append(RANDOM.nextInt(10));
        }
        ref.append("-");

        // 4 alphanumeric
        for (int i = 0; i < 3; i++) {
            ref.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }

        return ref.toString();
    }
}
