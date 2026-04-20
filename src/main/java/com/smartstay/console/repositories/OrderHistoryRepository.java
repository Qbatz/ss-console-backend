package com.smartstay.console.repositories;

import com.smartstay.console.dao.OrderHistory;
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
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    Page<OrderHistory> findAllByIsActiveTrueAndOrderStatusInAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc
            (List<String> orderStatuses,
             Date startDate,
             Date endDate,
             Pageable pageable);

    @Query("""
                SELECT o FROM OrderHistory o
                WHERE o.isActive = true
                  AND o.createdAt >= :startDate
                  AND o.createdAt < :endDate
                  AND (
                       (:hostelIds IS NOT NULL AND o.hostelId IN :hostelIds)
                    OR (:userIds IS NOT NULL AND o.createdBy IN :userIds)
                  )
                  AND o.orderStatus IN :orderStatuses
                ORDER BY o.createdAt DESC
            """)
    Page<OrderHistory> findFilteredOrderHistory(@Param("hostelIds") Set<String> hostelIds,
                                                @Param("userIds") Set<String> userIds,
                                                @Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate,
                                                @Param("orderStatuses") List<String> orderStatuses,
                                                Pageable pageable);

    @Query("""
                SELECT coalesce(sum(o.totalAmount), 0) FROM OrderHistory o
                WHERE o.isActive = true
                  AND o.createdAt >= :startDate
                  AND o.createdAt < :endDate
                  AND o.orderStatus IN :orderStatuses
            """)
    double findTotalRevenueBetween(@Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate,
                                   @Param("orderStatuses") List<String> orderStatuses);
}
