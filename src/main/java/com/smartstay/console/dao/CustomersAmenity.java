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
public class CustomersAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String amenityId;
    private String customerId;
    private Double amenityPrice;
    private Date createdAt;
    private Date updatedAt;
    private String updatedBy;
    private String createdBy;
    private Date startDate;
    private Date endDate;
    private String reasonForStop;
}
