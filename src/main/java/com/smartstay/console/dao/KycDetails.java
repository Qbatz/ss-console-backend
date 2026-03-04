package com.smartstay.console.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KycDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String currentStatus;
    private String transactionId;
    private String referenceId;

    @OneToOne()
    @JoinColumn(name = "customer_id", referencedColumnName = "customerId")
    @JsonIgnore
    private Customers customers;
}
