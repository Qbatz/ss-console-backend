package com.smartstay.console.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity(name = "assetsv1")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetsV1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    private String assetName;
    private String productName;
    private int vendorId;
    private String brandName;
    private String serialNumber;
    private Date purchaseDate;
    private Double price;
    private String bankId;

    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private Date assignedAt;
    private Boolean isActive;
    private Boolean isDeleted;
    private String hostelId;
    private Integer floorId;
    private Integer roomId;
    private Integer bedId;
    private String parentId;
}