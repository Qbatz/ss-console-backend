package com.smartstay.console.events;

import com.smartstay.console.dto.hostel.BillingDates;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PostpaidRecurringEvents extends ApplicationEvent {

    private final String hostelId;
    private final int billingDay;
    private final BillingDates billingDates;

    public PostpaidRecurringEvents(Object source,
                                   String hostelId,
                                   int  billingDay,
                                   BillingDates billingDates) {
        super(source);
        this.hostelId = hostelId;
        this.billingDay = billingDay;
        this.billingDates = billingDates;
    }
}
