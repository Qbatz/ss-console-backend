package com.smartstay.console.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int addressId;
    private String houseNo;
    private String street;
    private String landMark;
    private String city;
    private String state;
    private int pincode;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private Users user;
}

