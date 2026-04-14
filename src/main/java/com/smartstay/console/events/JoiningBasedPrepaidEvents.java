package com.smartstay.console.events;

import com.smartstay.console.dto.hostel.BillingDates;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class JoiningBasedPrepaidEvents extends ApplicationEvent {

    private final String customerId;
    private final String hostelId;
    private final int billingDay;
    private final BillingDates billingDates;

    public JoiningBasedPrepaidEvents(Object source,
                                     String customerId,
                                     String hostelId,
                                     int billingDay,
                                     BillingDates billingDates) {
        super(source);
        this.customerId = customerId;
        this.hostelId = hostelId;
        this.billingDay = billingDay;
        this.billingDates = billingDates;
    }
}
