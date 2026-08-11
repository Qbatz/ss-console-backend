package com.smartstay.console.services;

import com.smartstay.console.dao.BankingV2;
import com.smartstay.console.repositories.BankingV2Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
