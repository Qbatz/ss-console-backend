package com.smartstay.console.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;
    private String parentId;
    private String firstName;
    private String lastName;
    private String mobileNo;
    private String emailId;
    @JsonIgnore
    private String password;
    private String profileUrl;
    private int roleId;
    private Long country;
    private String createdBy;
    private boolean twoStepVerificationStatus;
    private boolean emailAuthenticationStatus;
    private boolean smsAuthenticationStatus;
    private boolean isActive;
    private boolean isDeleted;
    private Date createdAt;
    private Date lastUpdate;
    private String description;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Address address;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private UsersConfig config;
}

