package com.smartstay.console.Mapper.customers;

import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.PaymentSummary;
import com.smartstay.console.responses.customers.CustomerSummaryResponse;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class CustomerSumMapper implements Function<Customers, CustomerSummaryResponse> {

    HostelV1 hostelV1;
    PaymentSummary paymentSummary;

    public CustomerSumMapper(HostelV1 hostelV1,
                             PaymentSummary paymentSummary) {
        this.hostelV1 = hostelV1;
        this.paymentSummary = paymentSummary;
    }

    @Override
    public CustomerSummaryResponse apply(Customers customers) {

        String hostelName = null;
        Double payableAmount = 0d;
        Double paidAmount = 0d;
        Double dueAmount = 0d;

        if (hostelV1 != null){
            hostelName = hostelV1.getHostelName();
        }

        if (paymentSummary != null){
            payableAmount = paymentSummary.getDebitAmount();
            paidAmount = paymentSummary.getCreditAmount();
            dueAmount = paymentSummary.getBalance();
        }

        String firstName = customers.getFirstName() != null ? customers.getFirstName().trim() : null;
        String lastName = customers.getLastName() != null ? customers.getLastName().trim() : null;

        return new CustomerSummaryResponse(customers.getCustomerId(), firstName, lastName,
                Utils.getFullName(firstName, lastName), Utils.getInitials(firstName, lastName),
                customers.getMobile(), customers.getEmailId(), customers.getProfilePic(),
                customers.getHostelId(), hostelName, customers.getCurrentStatus(),
                payableAmount, paidAmount, dueAmount);
    }
}
