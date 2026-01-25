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
public class Agents {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String agentId;
    private String agentName;
    private boolean isActive;
    private Date createdAt;
    private String createdBy;
}
