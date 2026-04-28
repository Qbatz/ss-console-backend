package com.smartstay.console.repositories;

import com.smartstay.console.dao.PaymentSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PaymentSummaryRepository extends JpaRepository<PaymentSummary, Integer> {

    List<PaymentSummary> findAllByCustomerIdIn(Set<String> customerIds);

    List<PaymentSummary> findAllByHostelIdAndCustomerId(String hostelId, String customerId);

    List<PaymentSummary> findAllByHostelIdAndCustomerIdIn(String hostelId, List<String> customerIds);
}
