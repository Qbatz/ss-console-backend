package com.smartstay.console.services;

import com.smartstay.console.dao.VendorCategories;
import com.smartstay.console.repositories.VendorCategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorCategoriesService {

    @Autowired
    private VendorCategoriesRepository vendorCategoriesRepository;

    public List<VendorCategories> getByHostelId(String hostelId) {
        return vendorCategoriesRepository.findAllByHostelId(hostelId);
    }

    public void deleteAll(List<VendorCategories> listVendorCategories) {
        vendorCategoriesRepository.deleteAll(listVendorCategories);
    }
}
