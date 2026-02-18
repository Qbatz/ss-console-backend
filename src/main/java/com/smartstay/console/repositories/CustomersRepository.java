package com.smartstay.console.repositories;

import com.smartstay.console.dao.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, String> {
    int countByHostelId(String hostelId);
}
