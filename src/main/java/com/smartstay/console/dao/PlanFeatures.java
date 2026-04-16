package com.smartstay.console.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanFeatures {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String featureName;
    private Double price;
    private boolean isActive;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    @JsonIgnore
    private Plans plan;

    @JsonProperty("planId")
    public Long getPlanId() {
        return plan != null ? plan.getPlanId() : null;
    }
}
