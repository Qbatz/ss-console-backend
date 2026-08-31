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
public class ProductUpdateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productUpdateItemId;
    private String title;
    private String description;
    // from product update type enum
    private String updateType;
    // from product update module enum
    private String module;
    // from product update CTA enum
    private String cta;
    private String ctaLink;
    private boolean showCtaButton;
    private List<String> itemImages;
    private boolean isActive;
    private boolean isDeleted;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;

    // links product update table
    private Long productUpdateId;
}
