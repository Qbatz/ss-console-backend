package com.smartstay.console.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Modules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    //bundle names(eg, dashboard, add customer, announcements, update)
    private String moduleName;
}
