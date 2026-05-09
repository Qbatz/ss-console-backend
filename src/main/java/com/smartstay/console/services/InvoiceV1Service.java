package com.smartstay.console.services;

import com.smartstay.console.Mapper.invoice.InvoiceResponseMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.repositories.InvoiceV1Repository;
import com.smartstay.console.responses.invoice.InvoiceResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceV1Service {

    @Autowired
    private InvoiceV1Repository invoiceV1Repository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private HostelService hostelService;
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
    @Autowired
    @Lazy
    private CustomersService customersService;
    @Autowired
    private UsersService usersService;

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
                .findByHostelIdAndStartDate(hostelId, startDate);

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

    public List<InvoicesV1> getInvoicesByIds(Set<String> invoiceIds) {
        return invoiceV1Repository.findAllByInvoiceIdIn(invoiceIds);
    }

    public InvoicesV1 getInvoiceById(String invoiceId) {
        return invoiceV1Repository.findByInvoiceId(invoiceId);
    }

    public void save(InvoicesV1 invoice) {
        invoiceV1Repository.save(invoice);
    }

    public List<InvoicesV1> getLimitedInvoicesByHostelId(String hostelId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        return invoiceV1Repository.findAllByHostelIdOrderByCreatedAtDesc(hostelId, pageable)
                .getContent();
    }

    public ResponseEntity<?> getInvoicesByHostelId(String hostelId, int page, int size) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Invoices.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelService.getHostelInfo(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        Page<InvoicesV1> pagedInvoices = invoiceV1Repository
                .findAllByHostelIdOrderByCreatedAtDesc(hostelId, pageable);

        List<InvoicesV1> invoices = pagedInvoices.getContent();

        Set<String> invoiceCustomerIds = new HashSet<>();
        Set<String> userIds = new HashSet<>();

        for (InvoicesV1 invoice : invoices) {
            if (invoice.getCustomerId() != null) {
                invoiceCustomerIds.add(invoice.getCustomerId());
            }
            if (invoice.getCreatedBy() != null){
                userIds.add(invoice.getCreatedBy());
            }
            if (invoice.getUpdatedBy() != null){
                userIds.add(invoice.getUpdatedBy());
            }
        }

        List<Customers> invoiceCustomers = customersService.getCustomersByIds(invoiceCustomerIds);

        Map<String, Customers> invoiceCustomerMap = invoiceCustomers.stream()
                .collect(Collectors.toMap(Customers::getCustomerId, customer -> customer));

        Map<String, Users> userMap = usersService.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(Users::getUserId, user -> user));

        List<InvoiceResponse> invoiceResponses = invoices.stream()
                .map(invoice -> {
                    Customers tenant = invoiceCustomerMap.getOrDefault(invoice.getCustomerId(), null);
                    Users createdByUser = userMap.getOrDefault(invoice.getCreatedBy(), null);
                    Users updatedByUser = userMap.getOrDefault(invoice.getCreatedBy(), null);
                    return new InvoiceResponseMapper(tenant, createdByUser, updatedByUser).apply(invoice);
                }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("invoiceList", invoiceResponses);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedInvoices.getTotalElements());
        response.put("totalPages", pagedInvoices.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
