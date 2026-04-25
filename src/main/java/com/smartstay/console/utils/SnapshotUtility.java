package com.smartstay.console.utils;

import com.smartstay.console.dao.*;
import com.smartstay.console.dto.agent.AgentSnapshot;
import com.smartstay.console.dto.customers.*;
import com.smartstay.console.dto.demoRequest.DemoRequestCommentsSnapshot;
import com.smartstay.console.dto.demoRequest.DemoRequestSnapshot;
import com.smartstay.console.dto.hostel.*;
import com.smartstay.console.dto.plans.PlanFeatureSnapshot;
import com.smartstay.console.dto.plans.PlanSnapshot;
import com.smartstay.console.dto.users.AddressSnapshot;
import com.smartstay.console.dto.users.UserSnapshot;
import com.smartstay.console.dto.users.UsersConfigSnapshot;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

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

    public static HostelPlanSnapshot toSnapshot(HostelPlan p) {
        if (p == null) return null;

        return new HostelPlanSnapshot(
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

    public static ElectricityConfigSnapshot toSnapshot(ElectricityConfig c) {
        if (c == null) return null;

        return new ElectricityConfigSnapshot(
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

    public static HostelImageSnapshot toSnapshot(HostelImages img) {
        if (img == null) return null;

        return new HostelImageSnapshot(
                img.getId(),
                img.getImageUrl(),
                img.getCreatedBy(),
                img.getHostel() != null ? img.getHostel().getHostelId() : null
        );
    }

    public static BillingRuleSnapshot toSnapshot(BillingRules r) {
        if (r == null) return null;

        return new BillingRuleSnapshot(
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
        );
    }

    public static HostelSnapshot toSnapshot(HostelV1 h) {

        if (h == null) return null;

        List<HostelImageSnapshot> images =
                h.getAdditionalImages() == null
                        ? List.of()
                        : h.getAdditionalImages().stream()
                        .map(SnapshotUtility::toSnapshot)
                        .toList();

        List<BillingRuleSnapshot> rules =
                h.getBillingRulesList() == null
                        ? List.of()
                        : h.getBillingRulesList().stream()
                        .map(SnapshotUtility::toSnapshot)
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
                toSnapshot(h.getHostelPlan()),
                toSnapshot(h.getElectricityConfig()),
                images,
                rules
        );
    }

    public static AddressSnapshot toSnapshot(Address a) {
        if (a == null) return null;

        return new AddressSnapshot(
                a.getAddressId(),
                a.getHouseNo(),
                a.getStreet(),
                a.getLandMark(),
                a.getCity(),
                a.getState(),
                a.getPincode(),
                a.getUser() != null ? a.getUser().getUserId() : null
        );
    }

    public static UsersConfigSnapshot toSnapshot(UsersConfig c) {
        if (c == null) return null;

        return new UsersConfigSnapshot(
                c.getConfigId(),
                c.getFcmToken(),
                c.getFcmWebToken(),
                c.getUser() != null ? c.getUser().getUserId() : null
        );
    }

    public static UserSnapshot toSnapshot(Users u) {

        if (u == null) return null;

        return new UserSnapshot(
                u.getUserId(),
                u.getParentId(),
                u.getFirstName(),
                u.getLastName(),
                u.getMobileNo(),
                u.getEmailId(),
                u.getProfileUrl(),
                u.getRoleId(),
                u.getCountry(),
                u.getCreatedBy(),
                u.isTwoStepVerificationStatus(),
                u.isEmailAuthenticationStatus(),
                u.isSmsAuthenticationStatus(),
                u.isActive(),
                u.isDeleted(),
                copyDate(u.getCreatedAt()),
                copyDate(u.getLastUpdate()),
                u.getDescription(),
                toSnapshot(u.getAddress()),
                toSnapshot(u.getConfig())
        );
    }

    public static AgentSnapshot toSnapshot(Agent a) {

        if (a == null) return null;

        return new AgentSnapshot(
                a.getAgentId(),
                a.getFirstName(),
                a.getLastName(),
                a.getMobile(),
                a.getAgentEmailId(),
                a.getRoleId(),
                a.getAgentZohoUserId(),
                a.getTicketLink(),
                a.getIsActive(),
                a.getIsProfileCompleted(),
                a.isMockAgent(),
                copyDate(a.getCreatedAt()),
                a.getCreatedBy(),
                copyDate(a.getUpdatedAt()),
                a.getUpdatedBy()
        );
    }

    public static CustomersCredentialsSnapshot toSnapshot(CustomerCredentials cc) {

        if (cc == null) return null;

        return new CustomersCredentialsSnapshot(
                cc.getXuid(),
                cc.getCustomerMobile(),
                cc.getCustomerPin(),
                cc.isPinVerified(),
                cc.getDefaultHostel(),
                cc.getFcmToken(),
                copyDate(cc.getCreatedAt())
        );
    }

    public static DeductionsSnapshot toSnapshot(Deductions d) {
        if (d == null) return null;

        return new DeductionsSnapshot(
                d.getType(),
                d.getAmount()
        );
    }

    public static AdvanceSnapshot toSnapshot(Advance a) {
        if (a == null) return null;

        return new AdvanceSnapshot(
                a.getId(),
                a.getAdvanceAmount(),
                a.getPaidAmount(),
                copyDate(a.getInvoiceDate()),
                copyDate(a.getDueDate()),
                a.getStatus(),
                copyDate(a.getCreatedAt()),
                copyDate(a.getUpdatedAt()),
                a.getCreatedBy(),
                a.getDeductions() == null
                        ? List.of()
                        : a.getDeductions().stream()
                        .map(SnapshotUtility::toSnapshot)
                        .toList(),
                a.getCustomers() != null ? a.getCustomers().getCustomerId() : null
        );
    }

    public static KycDetailsSnapshot toSnapshot(KycDetails k) {
        if (k == null) return null;

        return new KycDetailsSnapshot(
                k.getId(),
                k.getCurrentStatus(),
                k.getTransactionId(),
                k.getReferenceId(),
                k.getCustomers() != null ? k.getCustomers().getCustomerId() : null
        );
    }

    public static CustomerWalletSnapshot toSnapshot(CustomerWallet w) {
        if (w == null) return null;

        return new CustomerWalletSnapshot(
                w.getWalletId(),
                w.getAmount(),
                copyDate(w.getTransactionDate()),
                w.getCustomers() != null ? w.getCustomers().getCustomerId() : null
        );
    }

    public static ReasonsSnapshot toSnapshot(Reasons r) {
        if (r == null) return null;

        return new ReasonsSnapshot(
                r.getReasonId(),
                r.getReasonType(),
                r.getReasonText(),
                copyDate(r.getCreatedAt()),
                copyDate(r.getUpdatedAt()),
                r.getCreatedBy(),
                r.getCustomers() != null ? r.getCustomers().getCustomerId() : null
        );
    }

    public static CustomersSnapshot toSnapshot(Customers c) {

        if (c == null) return null;

        AdvanceSnapshot advance = toSnapshot(c.getAdvance());
        KycDetailsSnapshot kyc = toSnapshot(c.getKycDetails());
        CustomerWalletSnapshot wallet = toSnapshot(c.getWallet());
        ReasonsSnapshot reasons = toSnapshot(c.getReasons());

        return new CustomersSnapshot(
                c.getCustomerId(),
                c.getXuid(),
                c.getFirstName(),
                c.getLastName(),
                c.getMobile(),
                c.getEmailId(),
                c.getHouseNo(),
                c.getStreet(),
                c.getLandmark(),
                c.getPincode(),
                c.getCity(),
                c.getState(),
                c.getCountry(),
                c.getProfilePic(),
                c.getCustomerBedStatus(),
                copyDate(c.getJoiningDate()),
                copyDate(c.getExpJoiningDate()),
                copyDate(c.getDateOfBirth()),
                c.getCurrentStatus(),
                c.getGender(),
                c.getKycStatus(),
                c.getCreatedBy(),
                c.getHostelId(),
                copyDate(c.getCreatedAt()),
                copyDate(c.getLastUpdatedAt()),
                c.getUpdatedBy(),
                c.getMobSerialNo(),

                advance,
                kyc,
                wallet,
                reasons
        );
    }

    public static DemoRequestCommentsSnapshot toSnapshot(DemoRequestComments c) {
        if (c == null) return null;

        return new DemoRequestCommentsSnapshot(
                c.getId(),
                c.getComment(),
                c.getCreatedByUserType(),
                c.getCreatedBy(),
                copyDate(c.getCreatedAt()),
                c.getDemoRequest() != null ? c.getDemoRequest().getRequestId() : null
        );
    }

    public static DemoRequestSnapshot toSnapshot(DemoRequest d) {

        if (d == null) return null;

        List<DemoRequestCommentsSnapshot> comments =
                toSnapshotList(d.getDemoRequestComments(), SnapshotUtility::toSnapshot);

        return new DemoRequestSnapshot(
                d.getRequestId(),
                d.getName(),
                d.getEmailId(),
                d.getContactNo(),
                d.getCountryCode(),
                d.getOrganization(),
                d.getNoOfHostels(),
                d.getNoOfTenant(),
                d.getCity(),
                d.getState(),
                d.getCountry(),
                d.getDemoRequestStatus(),
                d.getIsDemoCompleted(),
                d.getIsAssigned(),
                d.getAssignedTo(),
                d.getAssignedBy(),
                d.getPresentedBy(),
                d.getComments(),
                copyDate(d.getBookedFor()),
                d.getRequestedDate(),
                d.getRequestedTime(),
                copyDate(d.getPresentedAt()),
                comments
        );
    }

    private static Date copyDate(Date d) {
        return d != null ? new Date(d.getTime()) : null;
    }

    public static <T, R> List<R> toSnapshotList(List<T> list, Function<T, R> mapper) {
        if (list == null || list.isEmpty()) return List.of();
        return list.stream().map(mapper).toList();
    }
}
