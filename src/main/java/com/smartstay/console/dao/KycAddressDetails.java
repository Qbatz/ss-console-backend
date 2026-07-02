package com.smartstay.console.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KycAddressDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String address;
    private String locality;
    private String city;
    private String state;
    private String pincode;

    @OneToOne()
    @JoinColumn(name = "kyc_details_id", referencedColumnName = "id")
    private KycDetails kycDetails;
}
