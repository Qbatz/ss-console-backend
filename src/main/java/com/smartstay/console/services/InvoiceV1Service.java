package com.smartstay.console.services;

import com.smartstay.console.dao.*;
import com.smartstay.console.repositories.InvoiceV1Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InvoiceV1Service {

    @Autowired
    private InvoiceV1Repository invoiceV1Repository;
    @Autowired
    private CustomerWalletHistoryService customerWalletHistoryService;
    @Autowired
    private CreditDebitNotesService creditDebitNotesService;
    @Autowired
    private InvoiceDiscountsService invoiceDiscountsService;
    @Autowired
    private TransactionV1Service transactionV1Service;
    @Autowired
    private BankTransactionService bankTransactionService;
    @Autowired
    private BankingService bankingService;

    public List<InvoicesV1> findByListOfCustomers(String hostelId, List<String> customerIds) {
        return invoiceV1Repository.findByHostelIdAndCustomerIdIn(hostelId, customerIds);
    }

    public void deleteAllInvoices(List<InvoicesV1> invoicesList) {
        invoiceV1Repository.deleteAll(invoicesList);
    }

    public List<InvoicesV1> findAllByHostelIdAndCustomerId(String hostelId, String customerId){
        return invoiceV1Repository.findAllByHostelIdAndCustomerId(hostelId, customerId);
    }

    public void deleteInvoicesByHostelIdAndStartDate(String hostelId, Date startDate) {

        List<InvoicesV1> invoices = invoiceV1Repository
                .findAllByHostelIdAndInvoiceStartDate(hostelId, startDate);

        Set<String> invoiceIds = invoices.stream()
                .map(InvoicesV1::getInvoiceId)
                .collect(Collectors.toSet());

        List<CustomerWalletHistory> cwhList = customerWalletHistoryService
                .getByInvoiceIds(invoiceIds);
        List<CreditDebitNotes> creditDebitNotes = creditDebitNotesService
                .getByInvoiceIds(invoiceIds);
        List<InvoiceDiscounts> invoiceDiscounts = invoiceDiscountsService
                .getByInvoiceIds(invoiceIds);
        List<TransactionV1> transactions = transactionV1Service
                .getByInvoiceIds(invoiceIds);

        Set<String> transactionIds = transactions.stream()
                .map(TransactionV1::getTransactionId)
                .collect(Collectors.toSet());
        Set<String> bankIds = transactions.stream()
                .map(TransactionV1::getBankId)
                .collect(Collectors.toSet());

        List<BankTransactionsV1> bankTransactions = bankTransactionService
                .getTransactionsByTransactionIds(transactionIds);
        List<BankingV1> bankingList = bankingService.findByBankIds(bankIds);

        HashMap<String, Double> bankBalances = new HashMap<>();

        if (!transactions.isEmpty()) {
            transactions.forEach(item -> {
                if (bankBalances.containsKey(item.getBankId())) {
                    if (item.getType() == null) {
                        double amount = bankBalances.get(item.getBankId());
                        amount = amount + item.getPaidAmount();
                        bankBalances.put(item.getBankId(), amount);
                    }
                    else {
                        double amount = bankBalances.get(item.getBankId());
                        amount = amount  + (-1 * item.getPaidAmount());
                        bankBalances.put(item.getBankId(), amount);
                    }
                }
                else {
                    if (item.getType() == null) {
                        bankBalances.put(item.getBankId(), item.getPaidAmount());
                    }
                    else {
                        bankBalances.put(item.getBankId(), item.getPaidAmount() * -1);
                    }
                }
            });
        }

        if (bankingList != null && !bankingList.isEmpty()) {
            List<BankingV1> newBalanceAmounts = bankingList
                    .stream()
                    .map(i -> {
                        if (bankBalances.get(i.getBankId()) != null) {
                            double amount = bankBalances.get(i.getBankId());
                            i.setBalance(i.getBalance() - amount);
                        }

                        return i;
                    }).toList();
            bankingService.updateBankAccount(newBalanceAmounts);
        }

        customerWalletHistoryService.deleteAll(cwhList);
        creditDebitNotesService.deleteAll(creditDebitNotes);
        invoiceDiscountsService.deleteAll(invoiceDiscounts);
        transactionV1Service.deleteAll(transactions);
        bankTransactionService.deleteAll(bankTransactions);
        invoiceV1Repository.deleteAll(invoices);
    }
}
