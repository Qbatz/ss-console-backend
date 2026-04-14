package com.smartstay.console.events;

import com.smartstay.console.dto.hostel.BillingDates;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Date;

@Getter
public class RecurringEvents extends ApplicationEvent {

    private final String hostelId;
    private final int billingDay;
    private final Date billingCycleStartDate;
    private final BillingDates billingDates;

    public RecurringEvents(Object source,
                           String hostelId,
                           int  billingDay,
                           Date billingCycleStartDate,
                           BillingDates billingDates) {
        super(source);
        this.hostelId = hostelId;
        this.billingDay = billingDay;
        this.billingCycleStartDate = billingCycleStartDate;
        this.billingDates = billingDates;
    }
}
