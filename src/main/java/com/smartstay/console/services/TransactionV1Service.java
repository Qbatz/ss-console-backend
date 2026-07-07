package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.customers.Deductions;
import com.smartstay.console.dto.transaction.TransactionSnapshot;
import com.smartstay.console.ennum.*;
import com.smartstay.console.payloads.customers.CustomerMobilePayload;
import com.smartstay.console.repositories.TransactionV1Repository;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class TransactionV1Service {

    @Autowired
    private TransactionV1Repository transactionV1Repository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private BankTransactionService bankTransactionService;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    @Lazy
    private InvoiceV1Service invoiceService;
    @Autowired
    @Lazy
    private CustomersService customersService;
    @Autowired
    private PaymentSummaryService paymentSummaryService;
    @Autowired
    private InvoiceRedemptionService invoiceRedemptionService;

    public List<TransactionV1> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return transactionV1Repository.findByHostelIdAndCustomerIdIn(hostelId, customerIds);
    }

    public void deleteAll(List<TransactionV1> listTransactions) {
        transactionV1Repository.deleteAll(listTransactions);
    }

    public List<TransactionV1> findByHostelIdAndCustomerId(String hostelId, String customerId) {
        return transactionV1Repository.findByHostelIdAndCustomerId(hostelId, customerId);
    }

    public List<TransactionV1> getByInvoiceIds(Set<String> invoiceIds) {
        return transactionV1Repository.findAllByInvoiceIdIn(invoiceIds);
    }

    public List<TransactionV1> getByInvoiceId(String invoiceId) {
        return transactionV1Repository.findByInvoiceId(invoiceId);
    }

    public ResponseEntity<?> deleteTransactionById(String transactionId, CustomerMobilePayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Receipt.getId(), Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        TransactionV1 transaction = transactionV1Repository
                .findByTransactionId(transactionId);
        if (transaction == null) {
            return new ResponseEntity<>(Utils.TRANSACTION_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        BankTransactionsV1 bankTransaction = bankTransactionService
                .getTransactionByTransactionId(transactionId);
        if (bankTransaction == null) {
            return new ResponseEntity<>(Utils.BANK_TRANSACTION_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        BankingV1 bank = bankingService
                .getByBankId(transaction.getBankId());
        if (bank == null){
            return new ResponseEntity<>(Utils.BANK_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 invoice = invoiceService.getInvoiceById(transaction.getInvoiceId());
        if (invoice == null){
            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Customers customer = customersService.getCustomerInformation(transaction.getCustomerId());
        if (customer == null){
            return new ResponseEntity<>(Utils.NO_CUSTOMER_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!customer.getMobile().equals(payload.tenantMobile())){
            return new ResponseEntity<>(Utils.TENANT_MOBILE_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        PaymentSummary paymentSummary = paymentSummaryService.getSummaryByCustomerId(transaction.getCustomerId());
        if (paymentSummary == null){
            return new ResponseEntity<>(Utils.PAYMENT_SUMMARY_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        TransactionSnapshot oldSnapshot = SnapshotUtility.toSnapshot(transaction);

        double transactionPaidAmount = transaction.getPaidAmount() != null ? transaction.getPaidAmount() : 0;

        double transactionNewPaidAmount;
        if (transaction.getType() == null) {
            transactionNewPaidAmount = transactionPaidAmount;
        }
        else {
            transactionNewPaidAmount = (-1 * transactionPaidAmount);
        }

        double invoicePaidAmount = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : 0;
        double invoiceTotalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0;

        double invoiceNewPaidAmount = invoicePaidAmount - transactionNewPaidAmount;
        invoice.setPaidAmount(invoiceNewPaidAmount);

        if (Objects.equals(invoiceNewPaidAmount, invoiceTotalAmount)){
            invoice.setPaymentStatus(PaymentStatus.PAID.name());
        } else if (invoiceNewPaidAmount <= 0) {
            invoice.setPaymentStatus(PaymentStatus.PENDING.name());
        } else if (invoiceNewPaidAmount > 0 && invoiceNewPaidAmount < invoiceTotalAmount) {
            invoice.setPaymentStatus(PaymentStatus.PARTIAL_PAYMENT.name());
        }

        double deductionAmount = invoice.getDeductionAmount() != null ? invoice.getDeductionAmount() : 0;

        if (InvoiceType.ADVANCE.name().equals(invoice.getInvoiceType())) {

            if (deductionAmount > 0 && invoiceNewPaidAmount < deductionAmount) {

                List<Deductions> invoiceDeductions = invoice.getDeductions();

                if (invoiceDeductions != null && !invoiceDeductions.isEmpty()) {

                    // amount decreased
                    if (transactionNewPaidAmount > 0) {

                        double remainingToRollback = transactionNewPaidAmount;

                        List<Deductions> reversed = new ArrayList<>(invoiceDeductions);

                        for (int i = reversed.size() - 1;
                             i >= 0 && remainingToRollback > 0;
                             i--) {

                            Deductions deduction = reversed.get(i);

                            double paidAmount = deduction.getPaidAmount() != null
                                    ? deduction.getPaidAmount()
                                    : 0;

                            if (paidAmount <= 0) {
                                continue;
                            }

                            if (paidAmount >= remainingToRollback) {

                                deduction.setPaidAmount(
                                        paidAmount - remainingToRollback
                                );

                                remainingToRollback = 0;

                            } else {

                                deduction.setPaidAmount(0.0);

                                remainingToRollback -= paidAmount;
                            }
                        }

                        invoice.setDeductions(reversed);
                    }

                    // amount increased
                    else if (transactionNewPaidAmount < 0) {

                        double allocationAmount = Math.abs(transactionNewPaidAmount);

                        final double[] tempAmount = {allocationAmount};

                        List<Deductions> updated = invoiceDeductions.stream()
                                .map(deduction -> {

                                    double paidAmount = deduction.getPaidAmount() != null
                                            ? deduction.getPaidAmount()
                                            : 0;

                                    double totalAmount = deduction.getAmount() != null
                                            ? deduction.getAmount()
                                            : 0;

                                    if (paidAmount >= totalAmount) {
                                        return deduction;
                                    }

                                    if (tempAmount[0] <= 0) {
                                        return deduction;
                                    }

                                    double balanceAmount = totalAmount - paidAmount;

                                    if (tempAmount[0] >= balanceAmount) {

                                        deduction.setPaidAmount(totalAmount);

                                        tempAmount[0] -= balanceAmount;

                                    } else {

                                        deduction.setPaidAmount(
                                                paidAmount + tempAmount[0]
                                        );

                                        tempAmount[0] = 0;
                                    }

                                    return deduction;
                                })
                                .toList();

                        invoice.setDeductions(updated);
                    }

                    double updatedDeductionAmount = invoice.getDeductions()
                            .stream()
                            .mapToDouble(d ->
                                    d.getAmount() != null
                                            ? d.getAmount()
                                            : 0
                            )
                            .sum();

                    invoice.setDeductionAmount(updatedDeductionAmount);
                }
            }
        }

        List<InvoiceRedemption> invoiceRedemptions = invoiceRedemptionService
                .getInvoiceRedemptionBySourceInvoiceId(invoice.getInvoiceId());

        double redemptionAmount = invoiceRedemptions.stream()
                .mapToDouble(r -> r.getRedemptionAmount() != null ? r.getRedemptionAmount() : 0)
                .sum();

        double invoiceBalanceAmount = invoiceNewPaidAmount - deductionAmount - redemptionAmount;
        if (invoiceBalanceAmount < 0){
            invoiceBalanceAmount = 0;
        }

        invoice.setBalanceAmount(invoiceBalanceAmount);

        bank.setBalance(bank.getBalance() - transactionNewPaidAmount);

        double creditAmount = paymentSummary.getCreditAmount() != null ? paymentSummary.getCreditAmount() : 0;
        double balance = paymentSummary.getBalance() != null ? paymentSummary.getBalance() : 0;

        paymentSummary.setCreditAmount(creditAmount - transactionNewPaidAmount);
        paymentSummary.setBalance(balance + transactionNewPaidAmount);

        invoiceService.save(invoice);
        bankingService.save(bank);
        paymentSummaryService.save(paymentSummary);

        bankTransactionService.delete(bankTransaction);
        transactionV1Repository.delete(transaction);

        agentActivitiesService.createAgentActivity(agent, ActivityType.DELETE, Source.TRANSACTION,
                transactionId, oldSnapshot, null);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }
}
