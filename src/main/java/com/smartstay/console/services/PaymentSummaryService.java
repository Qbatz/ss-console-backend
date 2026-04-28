package com.smartstay.console.services;

import com.smartstay.console.dao.PaymentSummary;
import com.smartstay.console.repositories.PaymentSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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
}
