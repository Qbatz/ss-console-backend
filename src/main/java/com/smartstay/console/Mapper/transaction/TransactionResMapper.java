package com.smartstay.console.Mapper.transaction;

import com.smartstay.console.dao.*;
import com.smartstay.console.responses.transaction.TransactionResponse;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class TransactionResMapper implements Function<TransactionV1, TransactionResponse> {

    InvoicesV1 invoice;
    HostelV1 hostel;
    Customers customer;
    BankingV1 bank;
    Users createdByUser;
    Users updatedByUser;

    public TransactionResMapper(InvoicesV1 invoice,
                                HostelV1 hostel,
                                Customers customer,
                                BankingV1 bank,
                                Users createdByUser,
                                Users updatedByUser) {
        this.invoice = invoice;
        this.hostel = hostel;
        this.customer = customer;
        this.bank = bank;
        this.createdByUser = createdByUser;
        this.updatedByUser = updatedByUser;
    }

    @Override
    public TransactionResponse apply(TransactionV1 transaction) {

        String paymentDate = null;
        String paymentTime = null;
        String createdAtDate = null;
        String createdAtTime = null;
        String invoiceNumber = null;
        String invoiceType = null;
        String hostelName = null;
        String customerName = null;
        String accountHolderName = null;
        String createdByUserName = null;
        String updatedByUserName = null;

        if (transaction.getPaymentDate() != null) {
            paymentDate = Utils.dateToString(transaction.getPaymentDate());
            paymentTime = Utils.dateToTime(transaction.getPaymentDate());
        }

        if (transaction.getCreatedAt() != null) {
            createdAtDate = Utils.dateToString(transaction.getCreatedAt());
            createdAtTime = Utils.dateToTime(transaction.getCreatedAt());
        }

        if (invoice != null) {
            invoiceNumber = invoice.getInvoiceNumber();
            invoiceType = invoice.getInvoiceType();
        }

        if (hostel != null){
            hostelName = hostel.getHostelName();
        }

        if (customer != null){
            customerName = Utils.getFullName(customer.getFirstName(), customer.getLastName());
        }

        if (bank != null){
            accountHolderName = bank.getAccountHolderName();
        }

        if (createdByUser != null){
            createdByUserName = Utils.getFullName(createdByUser.getFirstName(), createdByUser.getLastName());
        }

        if (updatedByUser != null){
            updatedByUserName = Utils.getFullName(updatedByUser.getFirstName(), updatedByUser.getLastName());
        }

        return new TransactionResponse(transaction.getTransactionId(), transaction.getType(),
                transaction.getPaidAmount(), transaction.getStatus(), transaction.getInvoiceId(),
                invoiceNumber, invoiceType, transaction.getHostelId(), hostelName, transaction.getCustomerId(),
                customerName, paymentDate, paymentTime, transaction.getTransactionMode(),
                transaction.getTransactionReferenceId(), transaction.getReceiptUrl(), transaction.getBankId(),
                accountHolderName, transaction.getReferenceNumber(), transaction.getCreatedBy(), createdByUserName,
                transaction.getUpdatedBy(), updatedByUserName, createdAtDate, createdAtTime);
    }
}
