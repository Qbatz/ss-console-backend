package com.smartstay.console.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RecurringEvents extends ApplicationEvent {

    private final String hostelId;
    private final int billingDay;

    public RecurringEvents(Object source,
                           String hostelId,
                           int  billingDay) {
        super(source);
        this.hostelId = hostelId;
        this.billingDay = billingDay;
    }
}
