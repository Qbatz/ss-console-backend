package com.smartstay.console.events;

import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dto.hostel.BillingDates;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DraftJoinBasedPrepaidRecurringEvents extends ApplicationEvent {

    private final Customers customer;
    private final HostelV1 hostel;
    private final BillingDates billingDates;

    public DraftJoinBasedPrepaidRecurringEvents(Object source,
                                                Customers customer,
                                                HostelV1 hostel,
                                                BillingDates billingDates) {
        super(source);
        this.customer = customer;
        this.hostel = hostel;
        this.billingDates = billingDates;
    }
}
