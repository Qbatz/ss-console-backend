package com.smartstay.console.Mapper.customers;

import com.smartstay.console.dao.Customers;
import com.smartstay.console.responses.customers.CustomerResponse;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class CustomerResMapper implements Function<Customers, CustomerResponse> {
    @Override
    public CustomerResponse apply(Customers customers) {
        return new CustomerResponse(customers.getCustomerId(), customers.getFirstName(),
                customers.getLastName(), Utils.getFullName(customers.getFirstName(), customers.getLastName()),
                Utils.getInitials(customers.getFirstName(), customers.getLastName()), customers.getMobile(),
                customers.getEmailId(), customers.getCurrentStatus());
    }
}
