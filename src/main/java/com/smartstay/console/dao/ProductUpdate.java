package com.smartstay.console.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productUpdateId;
    private String title;
    private String description;
    private String version;
    private Date releaseDate;
    // from product update type enum
    private String updateType;
    // from product update platform enum
    private String platform;
    // from product update audience enum
    private String audience;
    private List<String> audienceIds;
    // from publish status enum
    private String publishStatus;
    private Date publishDateTime;
    private Date expiryDate;
    private boolean isActive;
    private boolean isDeleted;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
}
