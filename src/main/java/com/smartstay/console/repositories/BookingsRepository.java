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
}
