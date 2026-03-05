package com.smartstay.console.services;

import com.smartstay.console.dao.BankingV1;
import com.smartstay.console.repositories.BankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankingService {

    @Autowired
    private BankingRepository bankingRepository;

    public List<BankingV1> findByHostelId(String hostelId) {
        return bankingRepository.findByHostelId(hostelId);
    }

    public void updateBankAccount(List<BankingV1> newBalanceAmounts) {
        bankingRepository.saveAll(newBalanceAmounts);
    }
}
