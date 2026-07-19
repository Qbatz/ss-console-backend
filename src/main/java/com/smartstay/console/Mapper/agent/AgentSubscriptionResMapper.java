package com.smartstay.console.Mapper.agent;

import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.UserType;
import com.smartstay.console.responses.agents.AgentSubscriptionRes;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public class AgentSubscriptionResMapper implements Function<Subscription, AgentSubscriptionRes> {

    HostelV1 hostel;
    Map<String, Agent> agentMap;
    Map<String, Users> usersMap;
    Plans plan;

    public AgentSubscriptionResMapper(HostelV1 hostel,
                                      Map<String, Agent> agentMap,
                                      Map<String, Users> usersMap,
                                      Plans plan) {
        this.hostel = hostel;
        this.agentMap = agentMap;
        this.usersMap = usersMap;
        this.plan = plan;
    }

    @Override
    public AgentSubscriptionRes apply(Subscription subscription) {

        String hostelName = null;
        String hostelInitials = null;

        if (hostel != null){
            hostelName = hostel.getHostelName();
            hostelInitials = Utils.getInitials(hostelName);
        }

        String createdBy = null;
        if (subscription.getCreatedBy() != null){
            if (subscription.getCreatedByUserType() != null &&
                    subscription.getCreatedByUserType().equals(UserType.AGENT.name())) {
                Agent createdByAgent = agentMap.getOrDefault(subscription.getCreatedBy(), null);
                if (createdByAgent != null){
                    createdBy = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
                }
            } else {
                Users createdByUser = usersMap.getOrDefault(subscription.getCreatedBy(), null);
                if (createdByUser != null){
                    createdBy = Utils.getFullName(createdByUser.getFirstName(), createdByUser.getLastName());
                }
            }
        }

        boolean isExpired = false;
        Date today = new Date();
        today = Utils.getStartOfDay(today);
        if (!subscription.getPlanEndsAt().after(today)) {
            isExpired = true;
        }

        String planName = null;
        String planType = null;
        if (plan != null){
            planName = plan.getPlanName();
            planType = plan.getPlanType();
        }

        return new AgentSubscriptionRes(subscription.getSubscriptionId(), subscription.getSubscriptionNumber(),
                subscription.getOrderId(), subscription.getHostelId(), hostelName, hostelInitials,
                subscription.getPlanCode(), planName, planType, isExpired, Utils.dateToString(subscription.getPlanStartsAt()),
                Utils.dateToString(subscription.getPlanEndsAt()), subscription.getPlanAmount(), subscription.getPaidAmount(),
                subscription.getDiscount(), subscription.getDiscountAmount(), subscription.getPaymentProof(),
                subscription.getInvoiceUrl(), subscription.getCreatedByUserType(), createdBy,
                Utils.dateToString(subscription.getCreatedAt()), Utils.dateToTime(subscription.getCreatedAt()));
    }
}
