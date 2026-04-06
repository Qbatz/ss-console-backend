package com.smartstay.console.Mapper.orderHistory;

import com.smartstay.console.dao.*;
import com.smartstay.console.responses.orderHistory.OrderHistoryResponse;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class OrderHistoryMapper implements Function<OrderHistory, OrderHistoryResponse> {

    HostelV1 hostel;
    HotelType hotelType;
    Plans plan;
    Users createdByUser;

    public OrderHistoryMapper(HostelV1 hostel,
                              HotelType hotelType,
                              Plans plan,
                              Users createdByUser) {
        this.hostel = hostel;
        this.hotelType = hotelType;
        this.plan = plan;
        this.createdByUser = createdByUser;
    }

    @Override
    public OrderHistoryResponse apply(OrderHistory orderHistory) {

        String hostelName = null;
        String hostelInitials = null;
        String mobile = null;
        String houseNo = null;
        String street = null;
        String landmark = null;
        String city = null;
        String state = null;
        int country = 0;
        int pincode = 0;
        String fullAddress = null;
        String mainImage = null;
        if (hostel != null) {
            hostelName = hostel.getHostelName();
            hostelInitials = Utils.getInitials(hostelName);
            mobile = hostel.getMobile();
            houseNo = hostel.getHouseNo();
            street = hostel.getStreet();
            landmark = hostel.getLandmark();
            city = hostel.getCity();
            state = hostel.getState();
            country = hostel.getCountry();
            pincode = hostel.getPincode();
            mainImage = hostel.getMainImage();
            fullAddress = Utils.buildFullAddress(hostel);
        }

        String hostelType = null;
        if (hotelType != null){
            hostelType = hotelType.getType();
        }

        String planName = null;
        String planType = null;
        if (plan != null){
            planName = plan.getPlanName();
            planType = plan.getPlanType();
        }

        String upiId = null;
        if (orderHistory.getUpiId() != null){
            upiId = Utils.maskUpiId(orderHistory.getUpiId());
        }

        String cardNo = null;
        if (orderHistory.getCardNo() != null){
            cardNo = Utils.maskCardNo(orderHistory.getCardNo());
        }

        String createdBy = null;
        if (createdByUser != null){
            createdBy = Utils.getFullName(createdByUser.getFirstName(), createdByUser.getLastName());
        }

        return new OrderHistoryResponse(orderHistory.getHistoryId(), orderHistory.getHostelId(), hostelName,
                hostelInitials, hostelType, mobile, houseNo, street, landmark, city, state, country, pincode,
                fullAddress, mainImage, orderHistory.getDiscountAmount(), orderHistory.getPlanAmount(),
                orderHistory.getPlanCode(), planName, planType, orderHistory.getTotalAmount(), orderHistory.getOrderStatus(),
                orderHistory.getPaymentType(), orderHistory.getChannel(), upiId, orderHistory.getCardHolderName(),
                orderHistory.getCardType(), orderHistory.getCardBrand(), orderHistory.getIssuer(), cardNo,
                orderHistory.getUserType(), createdBy, Utils.dateToString(orderHistory.getCreatedAt()),
                Utils.dateToTime(orderHistory.getCreatedAt()));
    }
}
