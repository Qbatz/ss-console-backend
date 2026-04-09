package com.smartstay.console.repositories;

import com.smartstay.console.dao.CreditDebitNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditDebitNoteRepository extends JpaRepository<CreditDebitNotes, Integer> {

    @Query("""
            SELECT cdn FROM CreditDebitNotes cdn WHERE cdn.hostelId=:hostelId AND cdn.customerId IN (:customerIds)
            """)
    List<CreditDebitNotes> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds);

    List<CreditDebitNotes> findByHostelIdAndCustomerId(String hostelId, String customerId);
}
