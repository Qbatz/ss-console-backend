package com.smartstay.console.Mapper.subscription;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.Subscription;
import com.smartstay.console.dao.Users;
import com.smartstay.console.ennum.UserType;
import com.smartstay.console.responses.subscriptions.SubscriptionsResponse;
import com.smartstay.console.utils.Utils;

import java.util.Map;
import java.util.Date;
import java.util.function.Function;

public class SubscriptionsResMapper implements Function<Subscription, SubscriptionsResponse> {

    HostelV1 hostel;
    Map<String, Agent> agentMap;
    Map<String, Users> usersMap;

    public SubscriptionsResMapper(HostelV1 hostel,
                                  Map<String, Agent> agentMap,
                                  Map<String, Users> usersMap) {
        this.hostel = hostel;
        this.agentMap = agentMap;
        this.usersMap = usersMap;
    }

    @Override
    public SubscriptionsResponse apply(Subscription subscription) {

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

        Date today = new Date();
        boolean expired = Utils.compareWithTwoDates(subscription.getPlanEndsAt(), today) < 0;

        return new SubscriptionsResponse(subscription.getSubscriptionId(),
                subscription.getSubscriptionNumber(), subscription.getOrderId(), subscription.getHostelId(),
                hostelName, hostelInitials, subscription.getPlanCode(), subscription.getPlanName(),
                Utils.dateToString(subscription.getPlanStartsAt()), Utils.dateToString(subscription.getPlanEndsAt()),
                expired, subscription.getPlanAmount(), subscription.getPaidAmount(), subscription.getDiscount(),
                subscription.getDiscountAmount(), subscription.getPaymentProof(), subscription.getInvoiceUrl(),
                subscription.getCreatedByUserType(), createdBy, Utils.dateToString(subscription.getCreatedAt()),
                Utils.dateToTime(subscription.getCreatedAt()));
    }
}
