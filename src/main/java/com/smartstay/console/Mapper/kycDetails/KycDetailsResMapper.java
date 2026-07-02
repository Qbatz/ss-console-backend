package com.smartstay.console.Mapper.kycDetails;

import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.KycDetails;
import com.smartstay.console.responses.kycDetails.KycDetailsRes;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class KycDetailsResMapper implements Function<KycDetails, KycDetailsRes> {

    HostelV1 hostel;

    public KycDetailsResMapper(HostelV1 hostel) {
        this.hostel = hostel;
    }

    @Override
    public KycDetailsRes apply(KycDetails kycDetails) {

        Customers customer = kycDetails.getCustomers();

        String customerId = null;
        String customerFirstName = null;
        String customerLastName = null;
        String customerMobile = null;
        String customerFullName = null;
        if (customer != null) {
            customerId = customer.getCustomerId();
            customerFirstName = customer.getFirstName();
            customerLastName = customer.getLastName();
            customerMobile = customer.getMobile();
            customerFullName = Utils.getFullName(customer.getFirstName(), customer.getLastName());
        }

        String hostelId = null;
        String hostelName = null;
        String hostelMobile = null;
        String hostelInitials = null;
        String hostelMainImage = null;
        if (hostel != null) {
            hostelId = hostel.getHostelId();
            hostelName = hostel.getHostelName();
            hostelMobile = hostel.getMobile();
            hostelInitials = Utils.getInitials(hostel.getHostelName());
            hostelMainImage = hostel.getMainImage();
        }

        String expiringAtDate = null;
        String expiringAtTime = null;
        if (kycDetails.getExpireAt() != null) {
            expiringAtDate = Utils.dateToString(kycDetails.getExpireAt());
            expiringAtTime = Utils.dateToTime(kycDetails.getExpireAt());
        }

        String createdAtDate = null;
        String createdAtTime = null;
        if (kycDetails.getCreatedAt() != null) {
            createdAtDate = Utils.dateToString(kycDetails.getCreatedAt());
            createdAtTime = Utils.dateToTime(kycDetails.getCreatedAt());
        }

        String updatedAtDate = null;
        String updatedAtTime = null;
        if (kycDetails.getUpdatedAt() != null) {
            updatedAtDate = Utils.dateToString(kycDetails.getUpdatedAt());
            updatedAtTime = Utils.dateToTime(kycDetails.getUpdatedAt());
        }

        return new KycDetailsRes(kycDetails.getId(), customerId, customerFirstName, customerLastName,
                customerFullName, customerMobile, hostelId, hostelName, hostelMobile, hostelInitials,
                hostelMainImage, kycDetails.getCurrentStatus(), expiringAtDate, expiringAtTime,
                createdAtDate, createdAtTime, updatedAtDate, updatedAtTime);
    }
}
