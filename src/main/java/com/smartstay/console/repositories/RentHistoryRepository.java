package com.smartstay.console.repositories;

import com.smartstay.console.dao.RentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentHistoryRepository extends JpaRepository<RentHistory, Long> {

    List<RentHistory> findAllByCustomerId(String customerId);

    List<RentHistory> findAllByCustomerIdIn(List<String> customerIds);
}
