package com.smartstay.console.Mapper.orderHistory;

import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.UserType;
import com.smartstay.console.responses.orderHistory.OrderHistoryResponse;
import com.smartstay.console.utils.Utils;

import java.util.Map;
import java.util.function.Function;

public class OrderHistoryMapper implements Function<OrderHistory, OrderHistoryResponse> {

    HostelV1 hostel;
    HotelType hotelType;
    Plans plan;
    Map<String, Users> usersMap;
    Map<String, Agent> agentMap;

    public OrderHistoryMapper(HostelV1 hostel,
                              HotelType hotelType,
                              Plans plan,
                              Map<String, Users> usersMap,
                              Map<String, Agent> agentMap) {
        this.hostel = hostel;
        this.hotelType = hotelType;
        this.plan = plan;
        this.usersMap = usersMap;
        this.agentMap = agentMap;
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

        String paidBy = null;
        if (orderHistory.getPaidBy() != null){
            if (usersMap != null){
                Users paidByUser = usersMap.getOrDefault(orderHistory.getPaidBy(), null);
                if (paidByUser != null){
                    paidBy = Utils.getFullName(paidByUser.getFirstName(), paidByUser.getLastName());
                }
            }
        }

        String collectedBy = null;
        if (orderHistory.getCollectedBy() != null){
            if (agentMap != null){
                Agent collectedByAgent = agentMap.getOrDefault(orderHistory.getCollectedBy(), null);
                if (collectedByAgent != null){
                    collectedBy = Utils.getFullName(collectedByAgent.getFirstName(), collectedByAgent.getLastName());
                }
            }
        }

        String createdBy = null;
        if (orderHistory.getCreatedBy() != null && orderHistory.getUserType() != null){
            if (UserType.OWNER.name().equals(orderHistory.getUserType())){
                if (usersMap != null){
                    Users createdByUser = usersMap.getOrDefault(orderHistory.getCreatedBy(), null);
                    if (createdByUser != null){
                        createdBy = Utils.getFullName(createdByUser.getFirstName(), createdByUser.getLastName());
                    }
                }
            } else if (UserType.AGENT.name().equals(orderHistory.getUserType())) {
                if (agentMap != null){
                    Agent createdByAgent = agentMap.getOrDefault(orderHistory.getCreatedBy(), null);
                    if (createdByAgent != null){
                        createdBy = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
                    }
                }
            }
        }

        String paymentProofFileName = null;
        if (orderHistory.getPaymentProof() != null){
            paymentProofFileName = Utils.getBaseNameFromUrl(orderHistory.getPaymentProof());
        }

        return new OrderHistoryResponse(orderHistory.getHistoryId(), orderHistory.getHostelId(), hostelName,
                hostelInitials, hostelType, mobile, houseNo, street, landmark, city, state, country, pincode,
                fullAddress, mainImage, orderHistory.getDiscountAmount(), orderHistory.getPlanAmount(),
                orderHistory.getPlanCode(), planName, planType, orderHistory.getTotalAmount(), orderHistory.getOrderStatus(),
                orderHistory.getPaymentType(), orderHistory.getChannel(), orderHistory.getPaymentProof(), paymentProofFileName,
                upiId, orderHistory.getCardHolderName(), orderHistory.getCardType(), orderHistory.getCardBrand(),
                orderHistory.getIssuer(), cardNo, orderHistory.getUserType(), paidBy, collectedBy, createdBy,
                Utils.dateToString(orderHistory.getCreatedAt()), Utils.dateToTime(orderHistory.getCreatedAt()));
    }
}
