package com.smartstay.console.repositories;

import com.smartstay.console.dao.Customers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, String> {

    List<Customers> findAllByCustomerIdIn(Set<String> customerIds);

    Page<Customers> findAllByCustomerIdInOrderByJoiningDateDesc(Set<String> customerIds, Pageable pageable);

    Page<Customers> findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrderByCreatedAtDesc(String firstName,
                                                                                                             String lastName,
                                                                                                             Pageable pageable);

    Page<Customers> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Customers> findByHostelId(String hostelId);

    Customers findByCustomerIdAndHostelId(String customerId, String hostelId);

    List<Customers> findByCustomerIdIn(List<String> customerId);

    List<Customers> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String name, String name1);

    @Query("""
           SELECT c
           FROM Customers c
           WHERE COALESCE(c.joiningDate, c.expJoiningDate) IS NOT NULL
           AND FUNCTION('DAY', COALESCE(c.joiningDate, c.expJoiningDate)) IN :daySet
           """)
    List<Customers> findByDaySet(@Param("daySet") Set<Integer> daySet);
}
