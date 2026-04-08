package com.smartstay.console.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class JoiningBasedPrepaidEvents extends ApplicationEvent {

    private final String customerId;
    private final String hostelId;
    private final int billingDay;

    public JoiningBasedPrepaidEvents(Object source,
                                     String customerId,
                                     String hostelId,
                                     int billingDay) {
        super(source);
        this.customerId = customerId;
        this.hostelId = hostelId;
        this.billingDay = billingDay;
    }
}
