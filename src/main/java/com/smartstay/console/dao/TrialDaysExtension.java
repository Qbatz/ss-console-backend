package com.smartstay.console.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrialDaysExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long extensionId;
    private int duration;
    private String reason;
    private String remarks;
    private Long subscriptionId;
    private String createdByUserType;
    private String createdBy;
    private Date createdAt;
}
