package com.smartstay.console.services;

import com.smartstay.console.dao.BankTransactionsV1;
import com.smartstay.console.repositories.BankTransactionRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class BankTransactionService {

    @Autowired
    private BankTransactionRepositories bankTransactionRepositories;

    public List<BankTransactionsV1> getAllTransactions(String hostelId) {
        return bankTransactionRepositories.findByHostelId(hostelId);
    }

    public void deleteItemsOtherThanExpense(List<BankTransactionsV1> listItemsOtherThanExpense) {
        bankTransactionRepositories.deleteAll(listItemsOtherThanExpense);
    }

    public List<BankTransactionsV1> getTransactionsByTransactionIds(Set<String> transactionIds) {
        return bankTransactionRepositories.findByTransactionNumberIn(transactionIds);
    }

    public void updateBankTransactions(List<String> expensesId) {
        List<BankTransactionsV1> listBankTransactions = bankTransactionRepositories.findBySourceIdIn(expensesId);
        bankTransactionRepositories.deleteAll(listBankTransactions);
    }
}
