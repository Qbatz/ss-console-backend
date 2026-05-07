package com.smartstay.console.repositories;

import com.smartstay.console.dao.InvoiceRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface InvoiceRedemptionRepository extends JpaRepository<InvoiceRedemption, Long> {

    @Query("""
            select ir
            from InvoiceRedemption ir
            where (:hostelIds is null or ir.hostelId in :hostelIds)
            order by ir.id desc
            """)
    Page<InvoiceRedemption> findFilteredInvoiceRedemptions(Set<String> hostelIds,
                                                           Pageable pageable);

    List<InvoiceRedemption> findAllByHostelIdOrderByIdDesc(String hostelId);
}
