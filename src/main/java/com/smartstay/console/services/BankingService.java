package com.smartstay.console.services;

import com.smartstay.console.dao.BankingV1;
import com.smartstay.console.repositories.BankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public List<BankingV1> findByBankIds(Set<String> bankIds) {
        return bankingRepository.findByBankIdIn(bankIds);
    }

    public void removeExpenses(List<String> bankIds, HashMap<String, Double> expensePerBankIds) {
        List<BankingV1> bankingsList = bankingRepository.findByBankIdIn(new HashSet<>(bankIds));
        if (bankingsList != null) {

            List<BankingV1> newBankInfo = bankingsList
                    .stream()
                    .map(item -> {
                        double amount = expensePerBankIds.get(item.getBankId());
                        double bankBanlance = 0.0;
                        if (item.getBalance() != null) {
                            bankBanlance = item.getBalance();
                        }
                        amount = bankBanlance + amount;
                        item.setBalance(amount);

                        return item;
                    })
                    .toList();

            bankingRepository.saveAll(newBankInfo);
        }
    }
}
