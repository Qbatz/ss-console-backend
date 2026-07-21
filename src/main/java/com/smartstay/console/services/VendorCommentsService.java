package com.smartstay.console.services;

import com.smartstay.console.dao.VendorComments;
import com.smartstay.console.repositories.VendorCommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class VendorCommentsService {

    @Autowired
    private VendorCommentsRepository vendorCommentsRepository;

    public List<VendorComments> getByVendorIds(Set<Integer> vendorIds) {
        return vendorCommentsRepository.findAllByVendorIdIn(vendorIds);
    }

    public void deleteAll(List<VendorComments> vendorCommentsList) {
        vendorCommentsRepository.deleteAll(vendorCommentsList);
    }
}
