package com.smartstay.console.repositories;

import com.smartstay.console.dao.ExpensesV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRespository extends JpaRepository<ExpensesV1, String> {
    List<ExpensesV1> findByHostelId(String hostelId);
}
