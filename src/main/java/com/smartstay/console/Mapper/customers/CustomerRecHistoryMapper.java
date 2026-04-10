package com.smartstay.console.Mapper.customers;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.CustomerRecurringTracker;
import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.responses.customers.CustomerRecHistoryRes;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.function.Function;

public class CustomerRecHistoryMapper implements Function<CustomerRecurringTracker, CustomerRecHistoryRes> {

    Customers customer;
    HostelV1 hostel;
    Agent createdByAgent;

    public CustomerRecHistoryMapper(Customers customer,
                                    HostelV1 hostel,
                                    Agent createdByAgent) {
        this.customer = customer;
        this.hostel = hostel;
        this.createdByAgent = createdByAgent;
    }

    @Override
    public CustomerRecHistoryRes apply(CustomerRecurringTracker customerRecurringTracker) {

        String customerName = null;
        if(customer != null){
            customerName = Utils.getFullName(customer.getFirstName(), customer.getLastName());
        }

        String hostelName = null;
        if(hostel != null){
            hostelName = hostel.getHostelName();
        }

        String createdBy = null;
        if (createdByAgent != null){
            createdBy = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
        }

        int month = customerRecurringTracker.getCreationMonth();
        int year = customerRecurringTracker.getCreationYear();

        int startDay = customerRecurringTracker.getCreationDay();
        int endDay = Utils.getEndDay(startDay, month, year);

        Date startDate = Utils.getDateFromDay(startDay, month, year);
        Date endDate = Utils.getEndDate(startDay, month, year);

        return new CustomerRecHistoryRes(customerRecurringTracker.getTrackerId(), customerName, hostelName,
                customerRecurringTracker.getCreationDay(), customerRecurringTracker.getCreationMonth(),
                customerRecurringTracker.getCreationYear(), startDay, endDay, Utils.dateToString(startDate),
                Utils.dateToString(endDate), customerRecurringTracker.getMode(), createdBy,
                Utils.dateToString(customerRecurringTracker.getCreatedAt()), Utils.dateToTime(customerRecurringTracker.getCreatedAt()));
    }
}
