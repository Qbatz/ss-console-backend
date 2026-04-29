package com.smartstay.console.dao;

import com.smartstay.console.ennum.RelationalAgentReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HostelRelationalAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String hostelId;
    private String agentId;
    @Enumerated(EnumType.STRING)
    private RelationalAgentReason reason;
    private String comments;
    private String createdBy;
    private Date createdAt;
}
