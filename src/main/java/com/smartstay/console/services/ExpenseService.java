package com.smartstay.console.services;

import com.smartstay.console.dao.ExpensesV1;
import com.smartstay.console.repositories.ExpenseRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class ExpenseService {
    @Autowired
    private BankTransactionService bankTransactionService;
    @Autowired
    private ExpenseRespository expenseRespository;
    @Autowired
    private BankingService bankingService;

    public ResponseEntity<?> deleteExpenses(String hostelId) {
        List<ExpensesV1> listExpenses = expenseRespository.findByHostelId(hostelId);
        List<String> bankIds = listExpenses
                .stream()
                .map(ExpensesV1::getBankId)
                .distinct()
                .toList();
        List<String> expensesId = listExpenses
                .stream()
                .map(ExpensesV1::getExpenseId)
                .toList();
        HashMap<String, Double> expensePerBankIds = new HashMap<>();
        listExpenses.forEach(item -> {
            if (expensePerBankIds.containsKey(item.getBankId())) {
                double amount = expensePerBankIds.get(item.getBankId());
                if (item.getTransactionAmount() != null) {
                    amount = amount + item.getTransactionAmount();
                }
                expensePerBankIds.put(item.getBankId(), amount);
            }
            else {
                double amount = 0.0;
                if (item.getTransactionAmount() != null) {
                    amount = item.getTransactionAmount();
                }
                expensePerBankIds.put(item.getBankId(), amount);
            }
        });

        bankTransactionService.updateBankTransactions(expensesId);
        bankingService.removeExpenses(bankIds, expensePerBankIds);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);


    }
}
