package com.smartstay.console.repositories;

import com.smartstay.console.dao.InvoicesV1;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface InvoiceV1Repository extends JpaRepository<InvoicesV1, String> {

    @Query("""
            SELECT i FROM invoicesv1 i
            WHERE i.hostelId=:hostelId
                AND i.customerId IN (:customerIds)
            """)
    List<InvoicesV1> findByHostelIdAndCustomerIdIn(String hostelId, List<String> customerIds);

    List<InvoicesV1> findAllByHostelIdAndCustomerId(String hostelId, String customerId);

    @Query(value = """
                SELECT * FROM invoicesv1
                WHERE hostel_id=:hostelId AND invoice_number LIKE CONCAT(:prefix, '%')
                ORDER BY CAST(SUBSTRING(invoice_number, LENGTH(:prefix) + 2) AS UNSIGNED) DESC
                LIMIT 1
            """, nativeQuery = true)
    InvoicesV1 findLatestInvoiceByPrefix(@Param("prefix") String prefix,
                                         @Param("hostelId") String hostelId);

    @Query("""
            SELECT i FROM invoicesv1 i
            WHERE i.hostelId = :hostelId
                AND DATE(i.invoiceStartDate) >= DATE(:startDate)
                AND i.invoiceType in ('RENT', 'REASSIGN_RENT')
            """)
    List<InvoicesV1> findByHostelIdAndStartDate(@Param("hostelId") String hostelId,
                                                @Param("startDate") Date startDate);

    List<InvoicesV1> findAllByInvoiceIdIn(Set<String> invoiceIds);

    InvoicesV1 findByInvoiceId(String invoiceId);

    Page<InvoicesV1> findAllByHostelIdOrderByCreatedAtDesc(String hostelId, Pageable pageable);
}
