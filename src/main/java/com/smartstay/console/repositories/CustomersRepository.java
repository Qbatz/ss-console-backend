package com.smartstay.console.repositories;

import com.smartstay.console.dao.Customers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, String> {
    List<Customers> findAllByCustomerIdIn(Set<String> customerIds);

    Page<Customers> findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrderByCreatedAtDesc(String firstName,
                                                                                                             String lastName,
                                                                                                             Pageable pageable);

    Page<Customers> findAllByOrderByCreatedAtDesc(Pageable pageable);


}
