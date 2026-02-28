package com.smartstay.console.Mapper.subscription;

import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.Subscription;
import com.smartstay.console.responses.subscriptions.SubscriptionsResponse;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class SubscriptionsResMapper implements Function<Subscription, SubscriptionsResponse> {

    HostelV1 hostel;

    public SubscriptionsResMapper(HostelV1 hostel) {
        this.hostel = hostel;
    }

    @Override
    public SubscriptionsResponse apply(Subscription subscription) {

        String hostelName = null;
        String hostelInitials = null;

        if (hostel != null){
            hostelName = hostel.getHostelName();
            hostelInitials = Utils.getInitials(hostelName);
        }

        return new SubscriptionsResponse(subscription.getSubscriptionId(),
                subscription.getSubscriptionNumber(), subscription.getHostelId(),
                hostelName, hostelInitials, subscription.getPlanCode(), subscription.getPlanName(),
                Utils.dateToString(subscription.getPlanStartsAt()), Utils.dateToString(subscription.getPlanEndsAt()),
                subscription.getPlanAmount(), subscription.getPaidAmount());
    }
}
