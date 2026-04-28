package com.smartstay.console.services;

import com.smartstay.console.dao.VendorV1;
import com.smartstay.console.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    public void deleteAll(List<VendorV1> listVendors) {
        vendorRepository.deleteAll(listVendors);
    }

    public List<VendorV1> findByHostelId(String hostelId) {
        return vendorRepository.findAllByHostelId(hostelId);
    }
}
