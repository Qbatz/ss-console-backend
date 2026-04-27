package com.smartstay.console.repositories;

import com.smartstay.console.dao.VendorV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<VendorV1, Integer> {

    List<VendorV1> findAllByHostelId(String hostelId);
}
