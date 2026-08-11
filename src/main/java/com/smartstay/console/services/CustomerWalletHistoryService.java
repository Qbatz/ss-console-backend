package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.CustomerWalletHistory;
import com.smartstay.console.dao.CustomersEbHistory;
import com.smartstay.console.ennum.WalletBillingStatus;
import com.smartstay.console.ennum.WalletSource;
import com.smartstay.console.ennum.WalletTransactionType;
import com.smartstay.console.repositories.CustomersWalletHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class CustomerWalletHistoryService {

    @Autowired
    private CustomersWalletHistoryRepository customersWalletHistoryRepository;
    @Autowired
    private Authentication authentication;

    public List<CustomerWalletHistory> findByCustomerIds(List<String> customerIds) {
        return customersWalletHistoryRepository.findByCustomerIdIn(customerIds);
    }

    public void deleteAll(List<CustomerWalletHistory> listCustomersWallet) {
        customersWalletHistoryRepository.deleteAll(listCustomersWallet);
    }

    public List<CustomerWalletHistory> findByCustomerId(String customerId) {
        return customersWalletHistoryRepository.findByCustomerId(customerId);
    }

    public List<CustomerWalletHistory> getWalletListForRecurring(List<String> customerIds) {
        return customersWalletHistoryRepository.findByCustomerIdIn(customerIds);
    }

    public void saveAll(List<CustomerWalletHistory> customerWallets) {
        customersWalletHistoryRepository.saveAll(customerWallets);
    }

    public  List<CustomerWalletHistory> getAllInvoiceNotGeneratedWallets(String customerId) {
        List<CustomerWalletHistory> listCustomerWalletHistory = customersWalletHistoryRepository
                .findInvoiceNotGeneratedByCustomerId(customerId);
        if (listCustomerWalletHistory == null) {
            listCustomerWalletHistory = new ArrayList<>();
        }

        return listCustomerWalletHistory;
    }

    public List<CustomerWalletHistory> getByInvoiceIds(Set<String> invoiceIds) {
        return customersWalletHistoryRepository.findAllByInvoiceIdIn(invoiceIds);
    }

    public List<CustomerWalletHistory> getByCustomerWalletHistoryIds(Set<Long> cwhIds) {
        return customersWalletHistoryRepository.findAllByHistoryIdIn(cwhIds);
    }

    public List<CustomerWalletHistory> getAllInvoiceNotGeneratedWalletsByCustomerIds(Set<String> customerIds) {
        return customersWalletHistoryRepository.findInvoiceNotGeneratedByCustomerIds(customerIds);
    }

    public CustomerWalletHistory buildEbWalletHistory(String customerId, CustomersEbHistory history) {

        CustomerWalletHistory walletHistory = new CustomerWalletHistory();

        walletHistory.setCustomerId(customerId);
        walletHistory.setSourceType(WalletSource.ELECTRICITY.name());
        walletHistory.setAmount(history.getAmount());
        walletHistory.setTransactionDate(history.getEndDate());
        walletHistory.setTransactionType(WalletTransactionType.CREDIT.name());
        walletHistory.setBillingStatus(WalletBillingStatus.INVOICE_NOT_GENERATED.name());
        walletHistory.setSourceId(String.valueOf(history.getReadingId()));
        walletHistory.setBillStartDate(history.getStartDate());
        walletHistory.setBillEndDate(history.getEndDate());
        walletHistory.setCreatedAt(new Date());
        walletHistory.setCreatedBy(authentication.getName());

        return walletHistory;
    }
}
