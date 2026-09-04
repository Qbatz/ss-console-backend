package com.smartstay.console.repositories;

import com.smartstay.console.dao.HostelReadings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostelReadingRepository extends JpaRepository<HostelReadings, Long> {

    List<HostelReadings> findByHostelId(String hostelId);

    @Query("""
            SELECT hr
            FROM HostelReadings hr
            WHERE hr.hostelId = :hostelId
              AND hr.billStatus = 'INVOICE_NOT_GENERATED'
            """)
    List<HostelReadings> findAllInvoiceNotGeneratedReadings(String hostelId);
}
