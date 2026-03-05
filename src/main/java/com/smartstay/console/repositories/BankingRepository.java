package com.smartstay.console.repositories;

import com.smartstay.console.dao.BankingV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BankingRepository extends JpaRepository<BankingV1, String> {
    List<BankingV1> findByHostelId(String hostelId);

    List<BankingV1> findByBankIdIn(Set<String> bankIds);
}
