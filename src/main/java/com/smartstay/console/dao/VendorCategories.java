package com.smartstay.console.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "VendorCategories")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorCategories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;
    private String categoryName;
    private String hostelId;
    private String addedBy;
    private boolean isEnabled;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;
    private String modifiedBy;
}
