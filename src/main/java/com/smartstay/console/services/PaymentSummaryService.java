package com.smartstay.console.services;

import com.smartstay.console.dao.InvoicesV1;
import com.smartstay.console.dao.PaymentSummary;
import com.smartstay.console.ennum.PaymentStatus;
import com.smartstay.console.exceptions.BadRequestException;
import com.smartstay.console.repositories.PaymentSummaryRepository;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PaymentSummaryService {

    @Autowired
    private PaymentSummaryRepository paymentSummaryRepository;

    public List<PaymentSummary> getSummaryByCustomerIds(Set<String> customerIds) {
        return paymentSummaryRepository.findAllByCustomerIdIn(customerIds);
    }

    public List<PaymentSummary> findByHostelIdAndCustomerId(String hostelId, String customerId) {
        return paymentSummaryRepository.findAllByHostelIdAndCustomerId(hostelId, customerId);
    }

    public void deleteAll(List<PaymentSummary> listPaymentSummary) {
        paymentSummaryRepository.deleteAll(listPaymentSummary);
    }

    public List<PaymentSummary> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return paymentSummaryRepository.findAllByHostelIdAndCustomerIdIn(hostelId, customerIds);
    }

    public void saveAll(List<PaymentSummary> paymentSummaryList) {
        paymentSummaryRepository.saveAll(paymentSummaryList);
    }

    public PaymentSummary getSummaryByCustomerId(String customerId) {
        return paymentSummaryRepository.findByCustomerId(customerId);
    }

    public void save(PaymentSummary paymentSummary) {
        paymentSummaryRepository.save(paymentSummary);
    }

    public void updatePaymentSummaryByInvoices(List<InvoicesV1> invoices) {

        Set<String> customerIds = invoices.stream()
                .map(InvoicesV1::getCustomerId)
                .collect(Collectors.toSet());

        List<PaymentSummary> paymentSummaries =
                paymentSummaryRepository.findAllByCustomerIdIn(customerIds);

        Map<String, PaymentSummary> paymentSummaryMap = paymentSummaries.stream()
                .collect(Collectors.toMap(
                        PaymentSummary::getCustomerId,
                        payment -> payment,
                        (a, b) -> a
                ));

        List<PaymentSummary> paymentSummaryList = new ArrayList<>();

        for (InvoicesV1 invoice : invoices) {

            PaymentSummary paymentSummary = paymentSummaryMap.get(invoice.getCustomerId());

            if (paymentSummary == null) {
                throw new BadRequestException(Utils.PAYMENT_SUMMARY_NOT_FOUND);
            }

            String status = invoice.getPaymentStatus();

            // Ignore statuses
            if (PaymentStatus.CANCELLED.name().equals(status) ||
                    PaymentStatus.ADVANCE_IN_HAND.name().equals(status)
            ) {
                continue;
            }

            if (invoice.getPaidAmount() == null || invoice.getTotalAmount() == null ||
                    paymentSummary.getDebitAmount() == null || paymentSummary.getCreditAmount() == null ||
                    paymentSummary.getBalance() == null){
                throw new BadRequestException(Utils.INVALID_AMOUNT);
            }

            double total = invoice.getTotalAmount();
            double paid = invoice.getPaidAmount();
            double due = total - paid;

            paymentSummary.setDebitAmount(paymentSummary.getDebitAmount() - total);
            paymentSummary.setCreditAmount(paymentSummary.getCreditAmount() - paid);
            paymentSummary.setBalance(paymentSummary.getBalance() - due);

            paymentSummaryList.add(paymentSummary);
        }

        paymentSummaryRepository.saveAll(paymentSummaryList);
    }
}
