package com.smartstay.console.Mapper.subscription;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.Subscription;
import com.smartstay.console.responses.subscriptions.SubscriptionsResponse;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class SubscriptionsResMapper implements Function<Subscription, SubscriptionsResponse> {

    HostelV1 hostel;
    Agent createdByAgent;

    public SubscriptionsResMapper(HostelV1 hostel,
                                  Agent createdByAgent) {
        this.hostel = hostel;
        this.createdByAgent = createdByAgent;
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
        if (createdByAgent != null){
            createdBy = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
        }

        return new SubscriptionsResponse(subscription.getSubscriptionId(),
                subscription.getSubscriptionNumber(), subscription.getHostelId(),
                hostelName, hostelInitials, subscription.getPlanCode(), subscription.getPlanName(),
                Utils.dateToString(subscription.getPlanStartsAt()), Utils.dateToString(subscription.getPlanEndsAt()),
                subscription.getPlanAmount(), subscription.getPaidAmount(), subscription.getDiscount(),
                subscription.getDiscountAmount(), subscription.getPaymentProof(), createdBy,
                Utils.dateToString(subscription.getCreatedAt()), Utils.dateToTime(subscription.getCreatedAt()));
    }
}
