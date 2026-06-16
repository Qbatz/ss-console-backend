package com.smartstay.console.services;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.ExpensesV1;
import com.smartstay.console.dto.hostel.ExpensesSnapshot;
import com.smartstay.console.dto.hostel.ExpensesSnapshotWrapper;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.repositories.ExpenseRepository;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
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
    private ExpenseRepository expenseRepository;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;

    public ResponseEntity<?> deleteExpenses(String hostelId, Agent agent) {

        List<ExpensesV1> listExpenses = expenseRepository.findByHostelId(hostelId);

        if (listExpenses.isEmpty()) {
            return new ResponseEntity<>(Utils.NO_EXPENSES_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<ExpensesSnapshot> snapshots =
                SnapshotUtility.toSnapshotList(listExpenses, SnapshotUtility::toSnapshot);

        ExpensesSnapshotWrapper snapshotWrapper = new ExpensesSnapshotWrapper(snapshots);

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
        expenseRepository.deleteAll(listExpenses);

        agentActivitiesService.createAgentActivity(agent, ActivityType.SNAPSHOT_DELETE, Source.HOSTEL_EXPENSE,
                hostelId, snapshotWrapper, null);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public void deleteExpensesByHostelId(String hostelId) {
        List<ExpensesV1> listExpenses = expenseRepository.findByHostelId(hostelId);
        expenseRepository.deleteAll(listExpenses);
    }
}
