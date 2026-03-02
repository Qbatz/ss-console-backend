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
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomersBedHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int bedId;
    private int roomId;
    private int floorId;
    private String hostelId;
    private Date startDate;
    private Date endDate;
    private String customerId;
    private String changedBy;
    private String reason;
    private boolean isActive;
    private Date createdAt;
    //this is from CustomerBedType enum
    private String type;
    private double rentAmount;

}
