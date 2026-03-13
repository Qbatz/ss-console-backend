package com.smartstay.console.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
    private Date startFrom;
    private Date endTill;
    private Date createdAt;
    private String createdBy;

    @ManyToOne
    @JoinColumn(name = "hostel_id")
    @JsonIgnore
    private HostelV1 hostel;

}
