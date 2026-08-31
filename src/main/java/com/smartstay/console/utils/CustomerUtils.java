package com.smartstay.console.utils;

import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.KycDetails;

public class CustomerUtils {

    public static String getProfilePic(Customers customers) {

        if (customers != null) {

            if (customers.getProfilePic() != null) {
                return customers.getProfilePic();
            }

            KycDetails kycDetails = customers.getKycDetails();
            if (kycDetails != null) {
                if (kycDetails.getCurrentStatus() != null &&
                        kycDetails.getCurrentStatus().equalsIgnoreCase("VERIFIED")) {
                    if (kycDetails.getIdPic() != null) {
                        return kycDetails.getIdPic();
                    }
                }
            }
        }

        return null;
    }
}
