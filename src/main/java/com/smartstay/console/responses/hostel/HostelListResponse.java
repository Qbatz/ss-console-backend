package com.smartstay.console.responses.hostel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HostelListResponse {
    private String hostelName;
    private String agentName;
    private Long subscriptionId;
    private String subscriptionNumber;
    private String hostelId;
    private String planCode;
    private String planName;
    private Date planStartsAt;
    private Date planEndsAt;
    private Date activatedAt;
    private Double paidAmount;
    private Double planAmount;
    private Double discount;
    private Double discountAmount;
    private Date nextBillingAt;
    private Date createdAt;
}
