package com.smartstay.console.services;

import com.smartstay.console.Mapper.plans.PlanResMapper;
import com.smartstay.console.Mapper.plans.PlansDropdownResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.PlanFeatures;
import com.smartstay.console.dao.Plans;
import com.smartstay.console.dao.SmartstayFeatures;
import com.smartstay.console.dto.plans.PlanFeatureDto;
import com.smartstay.console.dto.plans.PlanSnapshot;
import com.smartstay.console.dto.plans.SmartstayFeaturesSnapshot;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.PlanType;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.plans.*;
import com.smartstay.console.repositories.PlansRepository;
import com.smartstay.console.responses.plans.*;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
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
    @Autowired
    private SmartstayFeatureService smartstayFeatureService;

    public Plans findTrialPlan(){
        return plansRepository.findTopByPlanTypeAndIsActiveTrueOrderByPlanIdAsc(PlanType.TRIAL.name());
    }

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

        List<SmartstayFeatures> allSmartstayFeatures = smartstayFeatureService
                .getAllSmartstayFeatures();

        List<SmartstayFeatures> commonFeatures = smartstayFeatureService
                .getAllCommonFeatures();

        Set<Long> planIds = plans.stream()
                .map(Plans::getPlanId)
                .collect(Collectors.toSet());

        List<PlanFeatures> planFeaturesList = planFeaturesService
                .findAllByPlanIds(planIds);

        Map<Long, List<PlanFeatures>> planFeaturesMap = planFeaturesList.stream()
                .collect(Collectors.groupingBy(PlanFeatures::getPlanId));

        List<PlansResponse> activePlans = plans.stream()
                .filter(Plans::isActive)
                .map(plan -> {
                    List<PlanFeatures> planFeatures = planFeaturesMap
                            .getOrDefault(plan.getPlanId(), Collections.emptyList());

                    return new PlanResMapper(allSmartstayFeatures, commonFeatures, planFeatures)
                            .apply(plan);
                })
                .toList();

        List<PlansResponse> inActivePlans = plans.stream()
                .filter(p -> !p.isActive())
                .map(plan -> {
                    List<PlanFeatures> planFeatures = planFeaturesMap
                            .getOrDefault(plan.getPlanId(), Collections.emptyList());

                    return new PlanResMapper(allSmartstayFeatures, commonFeatures, planFeatures)
                            .apply(plan);
                }).toList();

        PlansResponseWrapper response = new PlansResponseWrapper(activePlans, inActivePlans);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getPlanById(Long planId) {

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

        Plans plan = plansRepository.findByPlanId(planId);
        if (plan == null) {
            return new ResponseEntity<>(Utils.PLAN_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<SmartstayFeatures> allSmartstayFeatures = smartstayFeatureService
                .getAllSmartstayFeatures();

        List<SmartstayFeatures> commonFeatures = smartstayFeatureService
                .getAllCommonFeatures();

        List<PlanFeatures> planFeatures = planFeaturesService
                .findAllByPlanId(planId);

        PlansResponse response = new PlanResMapper(allSmartstayFeatures, commonFeatures, planFeatures)
                .apply(plan);

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

        Plans plan = plansRepository.findByPlanId(planId);
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

            Set<Long> smartstayFeatureIds = payload.planFeatures().stream()
                    .map(PlanFeaturesUpdatePayload::smartstayFeatureId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<SmartstayFeatures> smartstayFeatures = smartstayFeatureService
                    .getSmartstayFeaturesByIds(smartstayFeatureIds);

            Map<Long, SmartstayFeatures> smartstayFeaturesMap = smartstayFeatures.stream()
                    .collect(Collectors.toMap(SmartstayFeatures::getId, Function.identity()));

            List<PlanFeatures> planFeatures = planFeaturesService
                    .getBySmartstayFeatureIdsAndPlanId(smartstayFeatureIds, planId);

            Map<Long, PlanFeatures> planFeaturesMap = planFeatures.stream()
                    .collect(Collectors.toMap(PlanFeatures::getSmartstayFeatureId, p -> p));

            List<PlanFeatures> updatedPlanFeatures = new ArrayList<>();
            Set<Long> processedFeatureIds = new HashSet<>();

            for (PlanFeaturesUpdatePayload pfPayload : payload.planFeatures()) {

                SmartstayFeatures smartstayFeature = smartstayFeaturesMap.get(pfPayload.smartstayFeatureId());
                if (smartstayFeature == null) {
                    return new ResponseEntity<>(Utils.SMARTSTAY_FEATURE_NOT_FOUND, HttpStatus.BAD_REQUEST);
                }

                if (!processedFeatureIds.add(pfPayload.smartstayFeatureId())) {
                    return new ResponseEntity<>(Utils.DUPLICATE_SMARTSTAY_FEATURE, HttpStatus.BAD_REQUEST);
                }

                PlanFeatures planFeature = planFeaturesMap.get(pfPayload.smartstayFeatureId());

                if (planFeature != null) {

                    Date startsFrom = null;
                    Date endsAt = null;
                    String labelText = null;
                    String labelDescription = null;

                    if (pfPayload.startsFrom() != null){
                        startsFrom = Utils.localDateToDate(pfPayload.startsFrom());
                    }
                    if (pfPayload.endsAt() != null){
                        endsAt = Utils.localDateToDate(pfPayload.endsAt());
                        endsAt = Utils.getEndOfDay(endsAt);
                    }

                    if (startsFrom != null && endsAt != null && startsFrom.after(endsAt)) {
                        return new ResponseEntity<>(Utils.INVALID_FEATURE_DATE_RANGE, HttpStatus.BAD_REQUEST);
                    }

                    if (pfPayload.labelText() != null && !pfPayload.labelText().isBlank()){
                        labelText = pfPayload.labelText();
                    }
                    if (pfPayload.labelDescription() != null && !pfPayload.labelDescription().isBlank()){
                        labelDescription = pfPayload.labelDescription();
                    }

                    planFeature.setLabelText(labelText);
                    planFeature.setLabelDescription(labelDescription);
                    planFeature.setStartsFrom(startsFrom);
                    planFeature.setEndsAt(endsAt);
                    planFeature.setFeatureActive(pfPayload.isFeatureActive() != null
                            ? pfPayload.isFeatureActive() : planFeature.isFeatureActive());
                    planFeature.setActive(true);

                    updatedPlanFeatures.add(planFeature);
                } else {
                    planFeature = new PlanFeatures();

                    Date startsFrom = null;
                    Date endsAt = null;

                    if (pfPayload.startsFrom() != null){
                        startsFrom = Utils.localDateToDate(pfPayload.startsFrom());
                    }
                    if (pfPayload.endsAt() != null){
                        endsAt = Utils.localDateToDate(pfPayload.endsAt());
                        endsAt = Utils.getEndOfDay(endsAt);
                    }

                    if (startsFrom != null && endsAt != null && startsFrom.after(endsAt)) {
                        return new ResponseEntity<>(Utils.INVALID_FEATURE_DATE_RANGE, HttpStatus.BAD_REQUEST);
                    }

                    planFeature.setFeatureName(smartstayFeature.getFeatureName());
                    planFeature.setPrice(0d);
                    planFeature.setSmartstayFeatureId(smartstayFeature.getId());
                    planFeature.setLabelText(pfPayload.labelText());
                    planFeature.setLabelDescription(pfPayload.labelDescription());
                    planFeature.setStartsFrom(startsFrom);
                    planFeature.setEndsAt(endsAt);
                    planFeature.setFeatureActive(pfPayload.isFeatureActive() != null ? pfPayload.isFeatureActive() : true);
                    planFeature.setActive(true);
                    planFeature.setPlan(plan);

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

        String planCode;
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

            Set<Long> featureIds = payload.planFeatures().stream()
                    .map(PlanFeaturesPayload::smartstayFeatureId)
                    .collect(Collectors.toSet());

            Map<Long, SmartstayFeatures> featureMap = smartstayFeatureService
                            .getSmartstayFeaturesByIds(featureIds)
                            .stream()
                            .collect(Collectors.toMap(
                                    SmartstayFeatures::getId,
                                    Function.identity()
                            ));

            Set<Long> processedFeatureIds = new HashSet<>();

            for (PlanFeaturesPayload pfPayload : payload.planFeatures()) {

                if (pfPayload.smartstayFeatureId() == null) {
                    return new ResponseEntity<>(Utils.SMARTSTAY_FEATURE_ID_REQUIRED, HttpStatus.BAD_REQUEST);
                }

                SmartstayFeatures smartstayFeatures = featureMap.get(pfPayload.smartstayFeatureId());
                if (smartstayFeatures == null){
                    return new ResponseEntity<>(Utils.SMARTSTAY_FEATURE_NOT_FOUND, HttpStatus.BAD_REQUEST);
                }

                if (!processedFeatureIds.add(pfPayload.smartstayFeatureId())) {
                    return new ResponseEntity<>(Utils.DUPLICATE_SMARTSTAY_FEATURE, HttpStatus.BAD_REQUEST);
                }

                PlanFeatures planFeature = new PlanFeatures();

                Date startsFrom = null;
                Date endsAt = null;

                if (pfPayload.startsFrom() != null){
                    startsFrom = Utils.localDateToDate(pfPayload.startsFrom());
                }
                if (pfPayload.endsAt() != null){
                    endsAt = Utils.localDateToDate(pfPayload.endsAt());
                    endsAt = Utils.getEndOfDay(endsAt);
                }

                if (startsFrom != null && endsAt != null && startsFrom.after(endsAt)) {
                    return new ResponseEntity<>(Utils.INVALID_FEATURE_DATE_RANGE, HttpStatus.BAD_REQUEST);
                }

                planFeature.setFeatureName(smartstayFeatures.getFeatureName());
                planFeature.setPrice(0d);
                planFeature.setSmartstayFeatureId(smartstayFeatures.getId());
                planFeature.setLabelText(pfPayload.labelText());
                planFeature.setLabelDescription(pfPayload.labelDescription());
                planFeature.setStartsFrom(startsFrom);
                planFeature.setEndsAt(endsAt);
                planFeature.setFeatureActive(pfPayload.isFeatureActive() != null ? pfPayload.isFeatureActive() : true);
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

        plan = plansRepository.save(plan);

        PlanSnapshot newPlan = SnapshotUtility.toSnapshot(plan);

        agentActivitiesService.createAgentActivity(agent, ActivityType.REACTIVATE, Source.PLANS,
                String.valueOf(planId), null, newPlan);

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

    public Set<String> getAllPlanCodesAndPlanTypesNotIn(Set<String> planTypes) {
        List<Plans> plans = plansRepository.findAllByPlanTypeNotIn(planTypes);
        return plans.stream()
                .map(Plans::getPlanCode)
                .collect(Collectors.toSet());
    }

    public Set<String> getAllPlanCodesByPlanType(String planType) {
        List<Plans> plans = plansRepository.findAllByPlanType(planType);
        return plans.stream()
                .map(Plans::getPlanCode)
                .collect(Collectors.toSet());
    }

    public static List<PlanFeatureDto> mergeFeatures(List<SmartstayFeatures> allSmartstayFeatures,
                                                     List<SmartstayFeatures> commonFeatures,
                                                     List<PlanFeatures> planFeatures) {

        Map<Long, SmartstayFeatures> smartstayFeatureMap = allSmartstayFeatures.stream()
                .collect(Collectors.toMap(
                        SmartstayFeatures::getId,
                        Function.identity()
                ));

        Map<Long, PlanFeatures> featureOverrideMap = planFeatures.stream()
                .collect(Collectors.toMap(
                        PlanFeatures::getSmartstayFeatureId,
                        Function.identity(),
                        (a, b) -> a
                ));

        Set<Long> commonFeatureIds = commonFeatures.stream()
                .map(SmartstayFeatures::getId)
                .collect(Collectors.toSet());

        List<PlanFeatureDto> mergedFeatures = new ArrayList<>();

        // Merge common features
        for (SmartstayFeatures commonFeature : commonFeatures) {

            PlanFeatures override =
                    featureOverrideMap.get(commonFeature.getId());

            if (override != null) {

                if (!override.isFeatureActive()) {
                    continue;
                }

                mergedFeatures.add(
                        new PlanFeatureDto(
                                override.getId(),
                                commonFeature.getId(),
                                commonFeature.getFeatureName(),
                                commonFeature.isCommon(),
                                override.getPrice(),
                                true,
                                override.getLabelText(),
                                override.getLabelDescription(),
                                override.getStartsFrom() != null
                                        ? Utils.dateToString(override.getStartsFrom())
                                        : null,
                                override.getEndsAt() != null
                                        ? Utils.dateToString(override.getEndsAt())
                                        : null
                        )
                );

            } else {

                mergedFeatures.add(
                        new PlanFeatureDto(
                                null,
                                commonFeature.getId(),
                                commonFeature.getFeatureName(),
                                commonFeature.isCommon(),
                                0d,
                                true,
                                null,
                                null,
                                null,
                                null
                        )
                );
            }
        }

        // Add non-common plan features
        for (PlanFeatures planFeature : planFeatures) {

            if (!planFeature.isFeatureActive()) {
                continue;
            }

            if (commonFeatureIds.contains(planFeature.getSmartstayFeatureId())) {
                continue;
            }

            SmartstayFeatures smartstayFeature =
                    smartstayFeatureMap.get(planFeature.getSmartstayFeatureId());

            mergedFeatures.add(
                    new PlanFeatureDto(
                            planFeature.getId(),
                            planFeature.getSmartstayFeatureId(),
                            planFeature.getFeatureName(),
                            smartstayFeature != null && smartstayFeature.isCommon(),
                            planFeature.getPrice(),
                            true,
                            planFeature.getLabelText(),
                            planFeature.getLabelDescription(),
                            planFeature.getStartsFrom() != null
                                    ? Utils.dateToString(planFeature.getStartsFrom())
                                    : null,
                            planFeature.getEndsAt() != null
                                    ? Utils.dateToString(planFeature.getEndsAt())
                                    : null
                    )
            );
        }

        mergedFeatures.sort(
                Comparator.comparing(
                        PlanFeatureDto::smartStayFeatureId,
                        Comparator.nullsLast(Long::compareTo)
                )
        );

        return mergedFeatures;
    }

    public ResponseEntity<?> getSmartstayFeatures() {

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

        List<SmartstayFeatures> smartstayFeatures = smartstayFeatureService
                .getAllSmartstayFeatures();

        List<SmartstayFeaturesResponse> response = smartstayFeatures.stream()
                .map(feature -> new SmartstayFeaturesResponse(feature.getId(),
                        feature.getFeatureName(), feature.isCommon(), feature.getCreatedAt() != null ?
                        Utils.dateToString(feature.getCreatedAt()) : null, feature.getCreatedAt() != null ?
                        Utils.dateToTime(feature.getCreatedAt()) : null, feature.getUpdatedAt() != null ?
                        Utils.dateToString(feature.getUpdatedAt()) : null, feature.getUpdatedAt() != null ?
                        Utils.dateToTime(feature.getUpdatedAt()) : null))
                .collect(Collectors.toList());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> addSmartstayFeatures(SmartstayFeatureAddPayload payload) {

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

        Date today = new Date();

        SmartstayFeatures smartstayFeatures = new SmartstayFeatures();

        if (smartstayFeatureService.existsByFeatureName(payload.featureName())){
            return new ResponseEntity<>(Utils.FEATURE_NAME_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        smartstayFeatures.setFeatureName(payload.featureName());
        smartstayFeatures.setCommon(payload.isCommon() != null ? payload.isCommon() : false);
        smartstayFeatures.setActive(true);
        smartstayFeatures.setCreatedAt(today);
        smartstayFeatures.setUpdatedAt(today);

        smartstayFeatures = smartstayFeatureService.save(smartstayFeatures);

        SmartstayFeaturesSnapshot newSnapshot = SnapshotUtility.toSnapshot(smartstayFeatures);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.SMARTSTAY_FEATURES,
                String.valueOf(smartstayFeatures.getId()), null, newSnapshot);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
    }

    public ResponseEntity<?> updateSmartstayFeatures(Long smartstayFeatureId,
                                                     SmartstayFeatureEditPayload payload) {

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

        SmartstayFeatures smartstayFeatures = smartstayFeatureService
                .getSmartstayFeatureById(smartstayFeatureId);
        if (smartstayFeatures == null){
            return new ResponseEntity<>(Utils.SMARTSTAY_FEATURE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        SmartstayFeaturesSnapshot oldSnapshot = SnapshotUtility.toSnapshot(smartstayFeatures);

        Date today = new Date();

        if (payload.featureName() != null && !payload.featureName().isBlank()) {
            if (smartstayFeatureService.existsByFeatureNameAndNotInId(payload.featureName(), smartstayFeatureId)){
                return new ResponseEntity<>(Utils.FEATURE_NAME_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }
            smartstayFeatures.setFeatureName(payload.featureName());
            planFeaturesService.updatePlanFeatureNameBySmartstayFeatureId(
                    smartstayFeatureId, payload.featureName());
        }
        if (payload.isCommon() != null) {
            smartstayFeatures.setCommon(payload.isCommon());
        }
        smartstayFeatures.setUpdatedAt(today);

        smartstayFeatures = smartstayFeatureService.save(smartstayFeatures);

        SmartstayFeaturesSnapshot newSnapshot = SnapshotUtility.toSnapshot(smartstayFeatures);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.SMARTSTAY_FEATURES,
                String.valueOf(smartstayFeatures.getId()), oldSnapshot, newSnapshot);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteSmartstayFeatures(Long smartstayFeatureId) {

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

        SmartstayFeatures smartstayFeatures = smartstayFeatureService
                .getSmartstayFeatureById(smartstayFeatureId);
        if (smartstayFeatures == null){
            return new ResponseEntity<>(Utils.SMARTSTAY_FEATURE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (smartstayFeatures.isCommon()){
            return new ResponseEntity<>(Utils.COMMON_FEATURE_CAN_NOT_BE_DELETED, HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        SmartstayFeaturesSnapshot oldSnapshot = SnapshotUtility.toSnapshot(smartstayFeatures);

        List<PlanFeatures> planFeatures = planFeaturesService
                .getBySmartstayFeatureId(smartstayFeatureId);

        for (PlanFeatures planFeature : planFeatures) {
            if (planFeature.isFeatureActive()){
                return new ResponseEntity<>(Utils.PLAN_FEATURE_EXISTS, HttpStatus.BAD_REQUEST);
            }
        }

        smartstayFeatures.setActive(false);
        smartstayFeatures.setUpdatedAt(today);

        smartstayFeatures = smartstayFeatureService.save(smartstayFeatures);

        agentActivitiesService.createAgentActivity(agent, ActivityType.DELETE, Source.SMARTSTAY_FEATURES,
                String.valueOf(smartstayFeatures.getId()), oldSnapshot, null);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }
}
