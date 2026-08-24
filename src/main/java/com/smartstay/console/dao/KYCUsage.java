package com.smartstay.console.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity(name = "kycusage")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class KYCUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usageId;
    private String hostelId;
    private String latestRequestBy;
    private String latestCompletionBy;
    private Integer requestCount;
    private Integer verifiedCount;
    private Date latestRequest;
    private Date latestVerified;
}
