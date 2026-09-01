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

    @Query("""
            SELECT o FROM OrderHistory o
            WHERE o.isActive = true
              AND COALESCE(o.paidAt, o.createdAt) >= :startDate
              AND COALESCE(o.paidAt, o.createdAt) < :endDate
              AND (
                   (:hostelIds IS NOT NULL AND o.hostelId IN :hostelIds)
                OR (:userIds IS NOT NULL AND o.paidBy IN :userIds)
              )
            ORDER BY COALESCE(o.paidAt, o.createdAt) DESC
            """)
    Page<OrderHistory> findFilteredOrderHistory(@Param("hostelIds") Set<String> hostelIds,
                                                @Param("userIds") Set<String> userIds,
                                                @Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate,
                                                Pageable pageable);

    @Query("""
            SELECT o FROM OrderHistory o
            WHERE o.isActive = true
              AND o.orderStatus = :orderStatus
              AND COALESCE(o.paidAt, o.createdAt) >= :startDate
              AND COALESCE(o.paidAt, o.createdAt) < :endDate
              AND (
                   (:hostelIds IS NOT NULL AND o.hostelId IN :hostelIds)
                OR (:userIds IS NOT NULL AND o.paidBy IN :userIds)
              )
            ORDER BY COALESCE(o.paidAt, o.createdAt) DESC
            """)
    Page<OrderHistory> findStatusFilteredOrderHistory(@Param("hostelIds") Set<String> hostelIds,
                                                      @Param("userIds") Set<String> userIds,
                                                      @Param("startDate") Date startDate,
                                                      @Param("endDate") Date endDate,
                                                      @Param("orderStatus") String orderStatus,
                                                      Pageable pageable);

    @Query("""
            SELECT o
            FROM OrderHistory o
            WHERE o.isActive = true
              AND COALESCE(o.paidAt, o.createdAt) >= :startDate
              AND COALESCE(o.paidAt, o.createdAt) < :endDate
            ORDER BY COALESCE(o.paidAt, o.createdAt) DESC
            """)
    Page<OrderHistory> findAllByPaidOrCreatedDate(@Param("startDate") Date startDate,
                                                  @Param("endDate") Date endDate,
                                                  Pageable pageable);

    @Query("""
            SELECT o
            FROM OrderHistory o
            WHERE o.isActive = true
              AND o.orderStatus = :orderStatus
              AND COALESCE(o.paidAt, o.createdAt) >= :startDate
              AND COALESCE(o.paidAt, o.createdAt) < :endDate
            ORDER BY COALESCE(o.paidAt, o.createdAt) DESC
            """)
    Page<OrderHistory> findStatusAllByPaidOrCreatedDate(@Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate,
                                                        @Param("orderStatus") String orderStatus,
                                                        Pageable pageable);

    @Query("""
            SELECT coalesce(sum(o.totalAmount), 0) FROM OrderHistory o
            WHERE o.isActive = true
              AND COALESCE(o.paidAt, o.createdAt) >= :startDate
              AND COALESCE(o.paidAt, o.createdAt) < :endDate
              AND o.orderStatus IN :orderStatuses
            """)
    double findTotalRevenueBetween(@Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate,
                                   @Param("orderStatuses") List<String> orderStatuses);

    OrderHistory findByHistoryIdAndIsActiveTrue(Long orderHistoryId);

    OrderHistory findByPaymentUrlAndOrderStatusAndIsActiveTrue(String paymentLink, String name);
}
