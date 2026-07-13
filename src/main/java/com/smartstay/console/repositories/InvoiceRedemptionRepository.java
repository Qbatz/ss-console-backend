package com.smartstay.console.repositories;

import com.smartstay.console.dao.InvoiceRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface InvoiceRedemptionRepository extends JpaRepository<InvoiceRedemption, Long> {

    @Query("""
            select ir
            from InvoiceRedemption ir
            where (:hostelIds is null or ir.hostelId in :hostelIds)
                and ir.isActive = true
            order by ir.id desc
            """)
    Page<InvoiceRedemption> findFilteredInvoiceRedemptions(Set<String> hostelIds,
                                                           Pageable pageable);

    Page<InvoiceRedemption> findAllByHostelIdAndIsActiveTrueOrderByIdDesc(String hostelId, Pageable pageable);

    @Query("""
                SELECT ir
                FROM InvoiceRedemption ir
                WHERE (
                    ir.sourceInvoiceId IN :invoiceIds
                    OR ir.targetInvoiceId IN :invoiceIds
                )
                AND ir.isActive = true
                ORDER BY ir.id DESC
            """)
    List<InvoiceRedemption> findByInvoiceIds(@Param("invoiceIds") Set<String> invoiceIds);

    @Query("""
                SELECT ir
                FROM InvoiceRedemption ir
                WHERE (
                    ir.sourceInvoiceId = :invoiceId
                )
                AND ir.isActive = true
                ORDER BY ir.id DESC
            """)
    List<InvoiceRedemption> findBySourceInvoiceId(@Param("invoiceId") String invoiceId);

    @Query("""
                SELECT ir
                FROM InvoiceRedemption ir
                WHERE (
                    ir.targetInvoiceId = :invoiceId
                )
                AND ir.isActive = true
                ORDER BY ir.id DESC
            """)
    List<InvoiceRedemption> findByTargetInvoiceId(@Param("invoiceId") String invoiceId);
}
