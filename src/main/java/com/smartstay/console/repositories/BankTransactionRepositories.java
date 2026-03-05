package com.smartstay.console.repositories;

import com.smartstay.console.dao.BankTransactionsV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BankTransactionRepositories extends JpaRepository<BankTransactionsV1, Integer> {
    List<BankTransactionsV1> findByHostelId(String hostelId);

    List<BankTransactionsV1> findByTransactionNumberIn(Set<String> transactionIds);
}
