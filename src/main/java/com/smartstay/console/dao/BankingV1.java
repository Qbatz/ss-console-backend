package com.smartstay.console.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity(name = "bankingv1")
@Data
@Getter
@Setter
public class BankingV1 {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String bankId;
    String bankName;
    String accountNumber;
    String parentId;
    String ifscCode;
    String branchName;
    String branchCode;
    String accountHolderName;
    String transactionType;
    String upiId;
    String creditCardNumber;
    String debitCardNumber;
    String accountType;
    String description;
    String userId;
    String hostelId;
    Double balance;
    boolean isActive;
    boolean isDeleted;
    boolean isDefaultAccount;
    String createdBy;
    String updatedBy;
    Date createdAt;
    Date updatedAt;
}
