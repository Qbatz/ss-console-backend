package com.smartstay.console.repositories;

import com.smartstay.console.dao.ElectricityReadings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface ElectricityReadingRepository extends JpaRepository<ElectricityReadings, Integer> {

    List<ElectricityReadings> findByHostelId(String hostelId);

    @Query(value = """
            SELECT * FROM electricity_readings
            WHERE hostel_id = :hostelId
                and bill_status='INVOICE_NOT_GENERATED'
            """, nativeQuery = true)
    List<ElectricityReadings> listAllReadingsForGenerateInvoice(String hostelId);

    @Query(value = """
            SELECT * FROM electricity_readings
            WHERE hostel_id = :hostelId
                AND room_id = :roomId
                AND DATE(bill_start_date) <= DATE(:endDate)
                AND DATE(bill_end_date) >= DATE(:startDate)
                AND bill_status='INVOICE_NOT_GENERATED'
                AND is_first_entry=false
            """, nativeQuery = true)
    List<ElectricityReadings> findPendingReadingsBetweenDates(@Param("hostelId") String hostelId,
                                                              @Param("roomId") Integer roomId,
                                                              @Param("startDate") Date startDate,
                                                              @Param("endDate") Date endDate);

    @Query(value = """
            SELECT * FROM electricity_readings er
            WHERE er.entry_date = (
                SELECT MAX(er2.entry_date)
                FROM electricity_readings er2
                WHERE er2.room_id = er.room_id)
              AND hostel_id=:hostelId
              AND room_id in (:roomIds)
            ORDER BY er.entry_date DESC
            """, nativeQuery = true)
    List<ElectricityReadings> findLatestEntriesByHostelIdAndRoomIds(@Param("hostelId") String hostelId,
                                                                    @Param("roomIds") Set<Integer> roomIds);
}
