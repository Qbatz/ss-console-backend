package com.smartstay.console.services;

import com.smartstay.console.repositories.CustomersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomersService {

    @Autowired
    CustomersRepository customersRepository;

    public int getCountByHostelId(String hostelId){
        return customersRepository.countByHostelId(hostelId);
    }
}
