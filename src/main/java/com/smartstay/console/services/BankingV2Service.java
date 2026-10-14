package com.smartstay.console.services;

import com.smartstay.console.dao.BankingV1;
import com.smartstay.console.dao.BankingV2;
import com.smartstay.console.repositories.BankingV2Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BankingV2Service {

    @Autowired
    private BankingV2Repository bankingV2Repository;

    public List<BankingV2> getByBankIds(Set<String> bankIds) {
        return bankingV2Repository.findAllByBankIdIn(bankIds);
    }

    public void saveAll(List<BankingV2> bankingV2s) {
        bankingV2Repository.saveAll(bankingV2s);
    }

    public List<BankingV2> getByHostelId(String hostelId) {
        return bankingV2Repository.findAllByHostelId(hostelId);
    }

    public void deleteAll(List<BankingV2> bankingV2List) {
        bankingV2Repository.deleteAll(bankingV2List);
    }

    public List<BankingV2> getByUserIds(Set<String> userIds) {
        return bankingV2Repository.findAllByUserIdIn(userIds);
    }

    public BankingV2 getByBankId(String bankId) {
        return bankingV2Repository.findByBankIdAndIsActiveTrueAndIsDeletedFalse(bankId);
    }

    public void save(BankingV2 bank) {
        bankingV2Repository.save(bank);
    }

    public void removeExpenses(List<String> bankIds, HashMap<String, Double> expensePerBankIds) {

        List<BankingV2> bankingsList = bankingV2Repository
                .findAllByBankIdIn(new HashSet<>(bankIds));

        if (bankingsList != null) {

            List<BankingV2> newBankInfo = bankingsList
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

            bankingV2Repository.saveAll(newBankInfo);
        }
    }
}
