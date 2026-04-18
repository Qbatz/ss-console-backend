package com.smartstay.console.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartstay.console.converters.IntegerListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillingRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer billingStartDate;
    private Integer billDueDays;
    private Integer noticePeriod;
    private boolean isInitial;
    private boolean hasGracePeriod;
    private Integer gracePeriodDays;
    //From billing type enum
    private String typeOfBilling;
    //Billing model enum
    private String billingModel;

    @Convert(converter = IntegerListConverter.class)
    private List<Integer> reminderDays;

    private boolean shouldNotify;
    private Date startFrom;
    private Date endTill;
    private Date createdAt;
    private String createdBy;

    @ManyToOne
    @JoinColumn(name = "hostel_id")
    @JsonIgnore
    private HostelV1 hostel;
}