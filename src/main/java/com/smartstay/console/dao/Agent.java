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
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String agentId;
    private String firstName;
    private String lastName;
    private String mobile;
    private String agentEmailId;
    private Long roleId;
    private String agentZohoUserId;
    private String ticketLink;
    private Boolean isActive;
    private Boolean isProfileCompleted;
    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private String updatedBy;
}
