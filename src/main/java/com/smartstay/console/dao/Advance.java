package com.smartstay.console.dao;

import com.smartstay.console.converters.DeductionsConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Advance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private double advanceAmount;
    private double paidAmount;
    private Date invoiceDate;
    private Date dueDate;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = DeductionsConverter.class)
    private List<Deductions> deductions;

    @OneToOne()
    @JoinColumn(name = "customer_id", referencedColumnName = "customerId")
    private Customers customers;
}
