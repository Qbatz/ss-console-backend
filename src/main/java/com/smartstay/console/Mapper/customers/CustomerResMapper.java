package com.smartstay.console.Mapper.customers;

import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.KycDetails;
import com.smartstay.console.ennum.CustomerStatus;
import com.smartstay.console.ennum.KycStatus;
import com.smartstay.console.responses.customers.CustomerResponse;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class CustomerResMapper implements Function<Customers, CustomerResponse> {

    @Override
    public CustomerResponse apply(Customers customers) {

        String joiningDate = null;
        if (customers.getJoiningDate() != null) {
            joiningDate = Utils.dateToString(customers.getJoiningDate());
        }

        KycDetails kycDetails = customers.getKycDetails();

        String kycDetailsStatus = null;
        boolean canApproveKyc = false;
        if (kycDetails != null) {
            kycDetailsStatus = kycDetails.getCurrentStatus();
            if (KycStatus.WAITING_FOR_APPROVAL.name().equalsIgnoreCase(kycDetailsStatus)) {
                canApproveKyc = true;
            }
        }

        boolean canGenerateSettlement = false;
        if (CustomerStatus.NOTICE.name().equals(customers.getCurrentStatus())){
            canGenerateSettlement = true;
        }

        return new CustomerResponse(customers.getCustomerId(), customers.getFirstName(),
                customers.getLastName(), Utils.getFullName(customers.getFirstName(), customers.getLastName()),
                Utils.getInitials(customers.getFirstName(), customers.getLastName()), Utils.maskMobileNo(customers.getMobile()),
                customers.getEmailId(), customers.getCurrentStatus(), joiningDate, customers.getKycStatus(),
                kycDetailsStatus, canApproveKyc, canGenerateSettlement);
    }
}
