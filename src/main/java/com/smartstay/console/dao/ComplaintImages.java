package com.smartstay.console.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
public class ComplaintImages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String imageUrl;
    private String createdBy;
    private Date createdAt;
    private Date updatedAt;
    private Boolean isActive;
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "complaint_id")
    @JsonIgnore
    private ComplaintsV1 complaints;
}
