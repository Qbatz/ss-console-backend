package com.smartstay.console.utils;

import com.smartstay.console.dao.*;
import com.smartstay.console.dto.hostel.*;
import com.smartstay.console.dto.plans.PlanFeatureSnapshot;
import com.smartstay.console.dto.plans.PlanSnapshot;

import java.util.Date;
import java.util.List;

public class SnapshotUtility {

    public static PlanSnapshot toSnapshot(Plans p) {

        if (p == null) return null;

        List<PlanFeatureSnapshot> features =
                p.getFeaturesList() == null
                        ? List.of()
                        : p.getFeaturesList().stream()
                        .map(SnapshotUtility::toSnapshot
                        ).toList();

        return new PlanSnapshot(
                p.getPlanId(),
                p.getPlanName(),
                p.getPrice(),
                p.getDuration(),
                p.getDiscounts(),
                p.getPlanType(),
                p.getPlanCode(),
                p.isShouldShow(),
                p.isCanCustomize(),
                p.isActive(),
                copyDate(p.getCreatedAt()),
                copyDate(p.getUpdatedAt()),
                features
        );
    }

    public static PlanFeatureSnapshot toSnapshot(PlanFeatures pf) {
        if (pf == null) return null;

        return new PlanFeatureSnapshot(
                pf.getId(),
                pf.getFeatureName(),
                pf.getPrice(),
                pf.isActive(),
                pf.getPlan() != null ? pf.getPlan().getPlanId() : null
        );
    }

    public static HostelSnapshot toSnapshot(HostelV1 h) {

        if (h == null) return null;

        HostelPlanSnapshot plan = null;
        if (h.getHostelPlan() != null) {
            var p = h.getHostelPlan();
            plan = new HostelPlanSnapshot(
                    p.getHostelPlanId(),
                    p.getCurrentPlanCode(),
                    p.getCurrentPlanName(),
                    copyDate(p.getCurrentPlanStartsAt()),
                    copyDate(p.getCurrentPlanEndsAt()),
                    p.getCurrentPlanPrice(),
                    p.getPaidAmount(),
                    p.isTrial(),
                    copyDate(p.getTrialEndingAt()),
                    p.getHostel() != null ? p.getHostel().getHostelId() : null
            );
        }

        ElectricityConfigSnapshot config = null;
        if (h.getElectricityConfig() != null) {
            var c = h.getElectricityConfig();
            config = new ElectricityConfigSnapshot(
                    c.getId(),
                    c.isShouldIncludeInRent(),
                    c.getTypeOfReading(),
                    copyDate(c.getLastUpdate()),
                    c.getUpdatedBy(),
                    c.getCharge(),
                    c.getFlatCharge(),
                    c.isUpdated(),
                    c.getBillDate(),
                    c.getHostel() != null ? c.getHostel().getHostelId() : null
            );
        }

        List<HostelImageSnapshot> images = h.getAdditionalImages() == null
                ? List.of()
                : h.getAdditionalImages().stream()
                        .map(img -> new HostelImageSnapshot(
                                img.getId(),
                                img.getImageUrl(),
                                img.getCreatedBy(),
                                img.getHostel() != null ? img.getHostel().getHostelId() : null
                        ))
                        .toList();

        List<BillingRuleSnapshot> rules = h.getBillingRulesList() == null
                ? List.of()
                : h.getBillingRulesList().stream()
                        .map(r -> new BillingRuleSnapshot(
                                r.getId(),
                                r.getBillingStartDate(),
                                r.getBillDueDays(),
                                r.getNoticePeriod(),
                                r.isInitial(),
                                r.isHasGracePeriod(),
                                r.getGracePeriodDays(),
                                r.getTypeOfBilling(),
                                r.getBillingModel(),
                                r.isShouldNotify(),
                                copyDate(r.getStartFrom()),
                                copyDate(r.getEndTill()),
                                copyDate(r.getCreatedAt()),
                                r.getCreatedBy(),
                                r.getReminderDays() != null ? List.copyOf(r.getReminderDays()) : List.of(),
                                r.getHostel() != null ? r.getHostel().getHostelId() : null
                        ))
                        .toList();

        return new HostelSnapshot(
                h.getHostelId(),
                h.getHostelType(),
                h.getHostelName(),
                h.getMobile(),
                h.getEmailId(),
                h.getMainImage(),
                h.getHouseNo(),
                h.getStreet(),
                h.getLandmark(),
                h.getPincode(),
                h.getCity(),
                h.getState(),
                h.getCountry(),
                h.getParentId(),
                h.getCreatedBy(),
                copyDate(h.getCreatedAt()),
                copyDate(h.getUpdatedAt()),
                h.isActive(),
                h.isDeleted(),
                plan,
                config,
                images,
                rules
        );
    }

    private static Date copyDate(Date d) {
        return d != null ? new Date(d.getTime()) : null;
    }
}
