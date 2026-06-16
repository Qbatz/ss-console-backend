package com.smartstay.console.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;
    @Column(unique = true, nullable = false)
    private String ticketNumber;
    private String parentId;
    private String hostelId;
    // user id
    private String raisedBy;
    // from query type enum
    private String queryType;
    private String subject;
    // from priority enum
    private String priority;
    private Date issueDate;
    private String assignedTo;
    private String assignedBy;
    private String remarks;
    private String paymentProof;
    // from support ticket source enum
    private String source;
    // from ticket status enum
    private String ticketStatus;
    private String createdByUserType;
    private String createdBy;
    private Date createdAt;
}
