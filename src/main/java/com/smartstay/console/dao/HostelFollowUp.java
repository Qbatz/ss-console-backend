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
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HostelFollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followUpId;
    private String hostelId;
    // from hostel follow-up status enum
    private String status;
    private String comments;
    // from hostel follow-up status enum reasons
    private String reason;
    private String createdBy;
    private Date createdAt;
}
