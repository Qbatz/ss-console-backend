package com.smartstay.console.repositories;

import com.smartstay.console.dao.ElectricityReadings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectricityReadingRepository extends JpaRepository<ElectricityReadings, Integer> {

    List<ElectricityReadings> findByHostelId(String hostelId);

    @Query(value = """
        SELECT * FROM electricity_readings WHERE hostel_id=:hostelId and bill_status='INVOICE_NOT_GENERATED'
        """, nativeQuery = true)
    List<ElectricityReadings> listAllReadingsForGenerateInvoice(String hostelId);
}
