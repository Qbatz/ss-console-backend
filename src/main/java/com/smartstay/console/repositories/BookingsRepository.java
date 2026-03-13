package com.smartstay.console.repositories;

import com.smartstay.console.dao.BookingsV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingsRepository extends JpaRepository<BookingsV1, String> {
    List<BookingsV1> findAllByHostelId(String hostelId);
    @Query("""
            SELECT b FROM bookingsv1 b WHERE b.hostelId=:hostelId AND b.customerId in (:customerIds)
            """)
    List<BookingsV1> findByHostelIdAndCustomerId(String hostelId, List<String> customerIds);

    List<BookingsV1> findAllByHostelIdAndCustomerId(String hostelId, String customerId);

    @Query(value = """
                SELECT * FROM bookingsv1 where hostel_id=:hostelId AND customer_id IN (:customerIds) AND current_status IN ('CHECKIN', 'NOTICE')
                """, nativeQuery = true)
    List<BookingsV1> findBookingsByListOfCustomersAndHostelId(List<String> customerIds, String hostelId);
}
