package com.smartstay.console.services;

import com.smartstay.console.Mapper.plans.InActivePlanResMapper;
import com.smartstay.console.Mapper.plans.PlanResMapper;
import com.smartstay.console.Mapper.plans.PlansDropdownResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.PlanFeatures;
import com.smartstay.console.dao.Plans;
import com.smartstay.console.dto.plans.PlanFeatureSnapshot;
import com.smartstay.console.dto.plans.PlanSnapshot;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.PlanType;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.plans.PlanFeaturesPayload;
import com.smartstay.console.payloads.plans.PlanFeaturesUpdatePayload;
import com.smartstay.console.payloads.plans.PlansPayload;
import com.smartstay.console.payloads.plans.PlansUpdatePayload;
import com.smartstay.console.repositories.PlansRepository;
import com.smartstay.console.responses.plans.PlansDropdownRes;
import com.smartstay.console.responses.plans.PlansDropdownWrapper;
import com.smartstay.console.responses.plans.PlansResponse;
import com.smartstay.console.responses.plans.PlansResponseWrapper;
import com.smartstay.console.utils.SnapshotUtility;
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

    public List<Plans> findTrialPlans() {
        return plansRepository.findAllByPlanTypeAndIsActiveTrue(PlanType.TRIAL.name());
    }

    public List<Plans> findExpandableTrialPlans() {
        return plansRepository.findAllByPlanTypeAndIsActiveTrue(PlanType.EXPANDABLE_TRIAL.name());
    }

    public Plans findPlanByPlanCode(String planCode) {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        return plansRepository.findByPlanCodeAndIsActiveTrue(planCode);
    }

    public List<Plans> findPlansByPlanCodes(Set<String> planCodes) {
        return plansRepository.findByPlanCodeInAndIsActiveTrue(planCodes);
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

        List<Plans> plans = plansRepository.findPlansExcludingTrial();

        Set<Long> inActivePlanIds = plans.stream()
                .filter(p -> !p.isActive())
                .map(Plans::getPlanId)
                .collect(Collectors.toSet());

        List<PlanFeatures> planFeatures = planFeaturesService.findAllByPlanIds(inActivePlanIds);

        Map<Long, List<PlanFeatures>> planFeaturesMap = planFeatures.stream()
                .collect(Collectors.groupingBy(PlanFeatures::getPlanId));

        List<PlansResponse> activePlans = plans.stream()
                .filter(Plans::isActive)
                .map(plan -> new PlanResMapper().apply(plan))
                .toList();

        List<PlansResponse> inActivePlans = plans.stream()
                .filter(p -> !p.isActive())
                .map(plan -> {
                    List<PlanFeatures> features = planFeaturesMap.getOrDefault(plan.getPlanId(), null);
                    return new InActivePlanResMapper(features).apply(plan);
                }).toList();

        PlansResponseWrapper response = new PlansResponseWrapper(activePlans, inActivePlans);

        return new ResponseEntity<>(response, HttpStatus.OK);
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

        double planPrice = plan.getPrice();
        double gstPercentage = plan.getGst();
        boolean isPriceOrGstUpdated = false;

        PlanSnapshot oldPlan = SnapshotUtility.toSnapshot(plan);

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
            if (payload.duration() <= 0){
                return new ResponseEntity<>(Utils.DURATION_NEED_TO_BE_HIGHER_THAN_ZERO, HttpStatus.BAD_REQUEST);
            }
            plan.setDuration(payload.duration());
        }
        if (payload.price() != null){
            if (payload.price() <= 0) {
                return new ResponseEntity<>(Utils.PRICE_SHOULD_BE_HIGHER_THAN_ZERO, HttpStatus.BAD_REQUEST);
            }
            plan.setPrice(payload.price());
            planPrice = payload.price();
            isPriceOrGstUpdated = true;
        }
        if (payload.discountPercentage() != null){
            if (payload.discountPercentage() < 0 || payload.discountPercentage() > 100){
                return new ResponseEntity<>(Utils.INVALID_DISCOUNT_PERCENTAGE, HttpStatus.BAD_REQUEST);
            }
            plan.setDiscounts(payload.discountPercentage());
        }
        if (payload.gstPercentage() != null){
            if (payload.gstPercentage() < 0 || payload.gstPercentage() > 100){
                return new ResponseEntity<>(Utils.INVALID_GST_PERCENTAGE, HttpStatus.BAD_REQUEST);
            }
            plan.setGst(payload.gstPercentage());
            gstPercentage = payload.gstPercentage();
            isPriceOrGstUpdated = true;
        }
        if (payload.shouldShow() != null) {
            plan.setShouldShow(payload.shouldShow());
        }
        if (payload.canCustomize() != null) {
            plan.setCanCustomize(payload.canCustomize());
        }
        plan.setUpdatedAt(new Date());

        if (isPriceOrGstUpdated){

            double halfGstPercentage = 0;
            double gstAmount = 0;
            double halfGstAmount = 0;

            if (gstPercentage != 0){
                halfGstPercentage = Utils.roundOfDoubleTo2Digits(gstPercentage / 2);

                gstAmount = Utils.roundOfDoubleTo2Digits((planPrice * gstPercentage) / 100);
                halfGstAmount = Utils.roundOfDoubleTo2Digits(gstAmount / 2);
            }

            double cgstPercentage = halfGstPercentage;
            double sgstPercentage = halfGstPercentage;
            double cgstAmount = halfGstAmount;
            double sgstAmount = halfGstAmount;

            double finalPrice = Utils.roundOfDoubleTo2Digits(planPrice + gstAmount);

            plan.setGstAmount(gstAmount);
            plan.setCgst(cgstPercentage);
            plan.setSgst(sgstPercentage);
            plan.setCgstAmount(cgstAmount);
            plan.setSgstAmount(sgstAmount);
            plan.setFinalPrice(finalPrice);
        }

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
                        if (pfPayload.price() < 0){
                            return new ResponseEntity<>(Utils.INVALID_PLAN_FEATURE_PRICE, HttpStatus.BAD_REQUEST);
                        }
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
                planCode = Utils.generatePlanCode();
            } while (plansRepository.existsByPlanCodeIgnoreCase(planCode));
        }
        plan.setPlanCode(planCode);

        plan.setShouldShow(payload.shouldShow());
        plan.setCanCustomize(payload.canCustomize());
        plan.setActive(true);
        plan.setCreatedAt(new Date());
        plan.setUpdatedAt(null);

        double gstPercentage = Utils.DEFAULT_GST_PERCENTAGE;
        if (payload.gstPercentage() != null){
            if (payload.gstPercentage() < 0 || payload.gstPercentage() > 100){
                return new ResponseEntity<>(Utils.INVALID_GST_PERCENTAGE, HttpStatus.BAD_REQUEST);
            }
            gstPercentage = payload.gstPercentage();
        }

        double halfGstPercentage = 0;
        double gstAmount = 0;
        double halfGstAmount = 0;

        if (gstPercentage != 0){
            halfGstPercentage = Utils.roundOfDoubleTo2Digits(gstPercentage / 2);

            gstAmount = Utils.roundOfDoubleTo2Digits((payload.price() * gstPercentage) / 100);
            halfGstAmount = Utils.roundOfDoubleTo2Digits(gstAmount / 2);
        }

        double cgstPercentage = halfGstPercentage;
        double sgstPercentage = halfGstPercentage;
        double cgstAmount = halfGstAmount;
        double sgstAmount = halfGstAmount;

        double finalPrice = Utils.roundOfDoubleTo2Digits(payload.price() + gstAmount);

        plan.setGst(gstPercentage);
        plan.setGstAmount(gstAmount);
        plan.setCgst(cgstPercentage);
        plan.setSgst(sgstPercentage);
        plan.setCgstAmount(cgstAmount);
        plan.setSgstAmount(sgstAmount);
        plan.setFinalPrice(finalPrice);

        List<PlanFeatures> planFeatures = new ArrayList<>();
        if (payload.planFeatures() != null && !payload.planFeatures().isEmpty()) {
            for (PlanFeaturesPayload pfPayload : payload.planFeatures()) {

                if (pfPayload.featureName() == null || pfPayload.featureName().isBlank()) {
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

        PlanSnapshot oldPlan = SnapshotUtility.toSnapshot(plan);

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

    public ResponseEntity<?> reactivatePlan(Long planId) {

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

        Plans plan = plansRepository.findByPlanIdAndIsActiveFalse(planId);
        if (plan == null) {
            return new ResponseEntity<>(Utils.PLAN_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        plan.setActive(true);
        plan.setUpdatedAt(new Date());

        List<PlanFeatures> planFeatures = planFeaturesService.findAllByPlanId(planId);

        for (PlanFeatures feature : planFeatures) {
            feature.setActive(true);
        }

        planFeaturesService.saveAll(planFeatures);
        plan = plansRepository.save(plan);

        PlanSnapshot newPlan = SnapshotUtility.toSnapshot(plan);

        agentActivitiesService.createAgentActivity(agent, ActivityType.REACTIVATE, Source.PLANS,
                String.valueOf(planId), null, newPlan);

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

        PlanFeatureSnapshot oldPlanFeature = SnapshotUtility.toSnapshot(planFeature);

        planFeature.setActive(false);

        planFeaturesService.save(planFeature);

        agentActivitiesService.createAgentActivity(agent, ActivityType.DEACTIVATE, Source.PLAN_FEATURES,
                String.valueOf(planFeatureId), oldPlanFeature, null);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public List<Plans> getFreePlans() {
        List<String> planTypes = new ArrayList<>();
        planTypes.add(PlanType.TRIAL.name());
        planTypes.add(PlanType.EXPANDABLE_TRIAL.name());
        List<Plans> listPlans = plansRepository.findByPlanTypeInAndIsActiveTrue(planTypes);
        if (listPlans == null) {
            listPlans = new ArrayList<>();
        }
        return listPlans;
    }

    public ResponseEntity<?> getPlansDropdown() {

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

        List<Plans> plansList = plansRepository.findAllByIsActiveTrue();

        List<PlansDropdownRes> trialPlans = new ArrayList<>();
        List<PlansDropdownRes> expandableTrialPlans = new ArrayList<>();
        List<PlansDropdownRes> otherPlans = new ArrayList<>();

        for (Plans plan : plansList) {
            if (PlanType.TRIAL.name().equals(plan.getPlanType())) {
                PlansDropdownRes plansDropdownRes = new PlansDropdownResMapper()
                        .apply(plan);
                trialPlans.add(plansDropdownRes);
            }
            else if (PlanType.EXPANDABLE_TRIAL.name().equals(plan.getPlanType())) {
                PlansDropdownRes plansDropdownRes = new PlansDropdownResMapper()
                        .apply(plan);
                expandableTrialPlans.add(plansDropdownRes);
            }
            else {
                PlansDropdownRes plansDropdownRes = new PlansDropdownResMapper()
                        .apply(plan);
                otherPlans.add(plansDropdownRes);
            }
        }

        PlansDropdownWrapper response = new PlansDropdownWrapper(trialPlans,
                expandableTrialPlans, otherPlans);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public Set<String> getAllPlanCodes() {
        List<Plans> plans = plansRepository.findAllByIsActiveTrue();
        return plans.stream()
                .map(Plans::getPlanCode)
                .collect(Collectors.toSet());
    }

    public Set<String> getAllPlanCodesAndPlanTypesNotIn(Set<String> planTypes) {
        List<Plans> plans = plansRepository.findAllByPlanTypeNotInAndIsActiveTrue(planTypes);
        return plans.stream()
                .map(Plans::getPlanCode)
                .collect(Collectors.toSet());
    }

    public Set<String> getAllPlanCodesByPlanType(String planType) {
        List<Plans> plans = plansRepository.findAllByPlanTypeAndIsActiveTrue(planType);
        return plans.stream()
                .map(Plans::getPlanCode)
                .collect(Collectors.toSet());
    }
}
