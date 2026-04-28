package com.smartstay.console.services;

import com.smartstay.console.dao.CustomersOtp;
import com.smartstay.console.repositories.CustomersOtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class CustomersOtpService {

    @Autowired
    private CustomersOtpRepository customersOtpRepository;

    public CustomersOtp findByXuid(String xuid) {
        return customersOtpRepository.findByXuid(xuid);
    }

    public void delete(CustomersOtp customersOtp) {
        customersOtpRepository.delete(customersOtp);
    }

    public List<CustomersOtp> findAllByXuids(Set<String> xuids) {
        return customersOtpRepository.findAllByXuidIn(xuids);
    }

    public void deleteAll(List<CustomersOtp> listCustomersOtp) {
        customersOtpRepository.deleteAll(listCustomersOtp);
    }
}
