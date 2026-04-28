package com.smartstay.console.repositories;

import com.smartstay.console.dao.BedChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BedChangeRequestRepository extends JpaRepository<BedChangeRequest, Long> {

    List<BedChangeRequest> findAllByHostelIdAndCustomerId(String hostelId, String customerId);

    List<BedChangeRequest> findAllByHostelIdAndCustomerIdIn(String hostelId, List<String> customerIds);
}
