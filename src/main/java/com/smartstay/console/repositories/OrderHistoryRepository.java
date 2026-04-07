package com.smartstay.console.repositories;

import com.smartstay.console.dao.OrderHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Set;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    Page<OrderHistory> findAllByIsActiveTrueAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(Date startDate,
                                                                                                                 Date endDate,
                                                                                                                 Pageable pageable);

    @Query("""
                SELECT o FROM OrderHistory o
                WHERE o.isActive = true
                  AND o.createdAt >= :startDate
                  AND o.createdAt < :endDate
                  AND (
                        (:hostelIds IS NULL OR o.hostelId IN :hostelIds)
                     OR (:userIds IS NULL OR o.createdBy IN :userIds)
                  )
                ORDER BY o.createdAt DESC
            """)
    Page<OrderHistory> findFilteredOrderHistory(@Param("hostelIds") Set<String> hostelIds,
                                                @Param("userIds") Set<String> userIds,
                                                @Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate,
                                                Pageable pageable);
}
