package com.smartstay.console.repositories;

import com.smartstay.console.dao.BankingV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BankingV2Repository extends JpaRepository<BankingV2, String> {
    
    List<BankingV2> findAllByBankIdIn(Set<String> bankIds);

    List<BankingV2> findAllByHostelId(String hostelId);
}
