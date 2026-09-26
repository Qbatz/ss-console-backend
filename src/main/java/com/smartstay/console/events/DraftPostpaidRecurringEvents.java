package com.smartstay.console.events;

import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dto.hostel.BillingDates;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DraftPostpaidRecurringEvents extends ApplicationEvent {

    private final HostelV1 hostel;
    private final BillingDates billingDates;

    public DraftPostpaidRecurringEvents(Object source,
                                        HostelV1 hostel,
                                        BillingDates billingDates) {
        super(source);
        this.hostel = hostel;
        this.billingDates = billingDates;
    }
}
