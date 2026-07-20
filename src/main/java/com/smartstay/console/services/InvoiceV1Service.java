package com.smartstay.console.services;

import com.smartstay.console.Mapper.invoice.InvoiceResponseMapper;
import com.smartstay.console.Mapper.transaction.TransactionResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.billTemplates.BillTemplatesDto;
import com.smartstay.console.dto.customers.Deductions;
import com.smartstay.console.dto.invoice.InvoiceSnapshot;
import com.smartstay.console.dto.invoice.InvoiceSnapshotWrapper;
import com.smartstay.console.dto.settlement.EBItems;
import com.smartstay.console.dto.settlement.WalltetItems;
import com.smartstay.console.ennum.*;
import com.smartstay.console.exceptions.BadRequestException;
import com.smartstay.console.payloads.invoice.AdvanceBalanceAmountPayload;
import com.smartstay.console.payloads.invoice.InvoiceIdMobilePayload;
import com.smartstay.console.repositories.InvoiceV1Repository;
import com.smartstay.console.responses.invoice.InvoiceResponse;
import com.smartstay.console.responses.transaction.TransactionResWrapper;
import com.smartstay.console.responses.transaction.TransactionResponse;
import com.smartstay.console.utils.SnapshotUtility;
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
    private AgentActivitiesService agentActivitiesService;
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
    @Autowired
    private InvoiceRedemptionService invoiceRedemptionService;
    @Autowired
    private PaymentSummaryService paymentSummaryService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private SettlementDetailsService settlementDetailsService;
    @Autowired
    private SettlementItemsService settlementItemsService;
    @Autowired
    private CustomerWalletService customerWalletService;
    @Autowired
    private CustomerBedHistoryService customerBedHistoryService;
    @Autowired
    private TemplatesService templatesService;

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

        deleteInvoices(invoiceIds, invoices);
    }

    private void deleteInvoices(Set<String> invoiceIds, List<InvoicesV1> invoices) {

        Set<String> cancelledInvoiceIds = invoices.stream()
                .filter(invoice -> InvoiceType.SETTLEMENT.name().equals(invoice.getInvoiceType()))
                .map(InvoicesV1::getCancelledInvoices)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        List<InvoicesV1> cancelledInvoices = invoiceV1Repository.findAllByInvoiceIdIn(cancelledInvoiceIds);

        Map<String, InvoicesV1> cancelledInvoiceMap = cancelledInvoices.stream()
                .collect(Collectors.toMap(InvoicesV1::getInvoiceId, invoice -> invoice));

        Set<String> customerIds = invoices.stream()
                .map(InvoicesV1::getCustomerId)
                .collect(Collectors.toSet());

        List<Customers> customers = customersService.getCustomersByIds(customerIds);

        Map<String, Customers> customersMap = customers.stream()
                .collect(Collectors.toMap(Customers::getCustomerId, customer -> customer));

        List<BookingsV1> bookings = bookingsService.getBookingsByCustomerIds(customerIds);

        Map<String, BookingsV1> bookingsMap = bookings.stream()
                .collect(Collectors.toMap(BookingsV1::getCustomerId, booking -> booking));

        Set<Integer> bedIds = bookings.stream()
                .map(BookingsV1::getBedId)
                .collect(Collectors.toSet());

        List<Beds> beds = bedsService.getBedsByBedIds(bedIds);

        Map<Integer, Beds> bedsMap = beds.stream()
                .collect(Collectors.toMap(Beds::getBedId, bed -> bed));

        List<SettlementDetails> settlementDetails = settlementDetailsService
                .findByCustomerIds(new ArrayList<>(customerIds));

        Map<String, SettlementDetails> settlementDetailsMap = settlementDetails.stream()
                .collect(Collectors.toMap(SettlementDetails::getCustomerId, sd -> sd));

        List<SettlementItems> settlementItems = settlementItemsService
                .getByInvoiceIds(invoiceIds);

        Map<String, SettlementItems> settlementItemsMap = settlementItems.stream()
                .collect(Collectors.toMap(SettlementItems::getInvoiceId, item -> item));

        Set<Long> cwhIds = settlementItems.stream()
                .map(SettlementItems::getWalltetItems)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(WalltetItems::getWalletId)
                .collect(Collectors.toSet());

        List<CustomerWalletHistory> customerWalletHistories = customerWalletHistoryService
                .getByCustomerWalletHistoryIds(cwhIds);

        Map<Long, CustomerWalletHistory> customerWalletHistoryMap = customerWalletHistories.stream()
                .collect(Collectors.toMap(CustomerWalletHistory::getHistoryId, cwh -> cwh));

        List<CustomersBedHistory> customersBedHistories = customerBedHistoryService
                .getLatestBedHistoriesByCustomerIds(customerIds);

        Map<String, CustomersBedHistory> customersBedHistoryMap = customersBedHistories.stream()
                .collect(Collectors.toMap(CustomersBedHistory::getCustomerId, cbh -> cbh));

        List<InvoicesV1> cancelledInvoicesList = new ArrayList<>();
        List<Customers> customersList = new ArrayList<>();
        List<BookingsV1> bookingsList = new ArrayList<>();
        List<Beds> bedsList = new ArrayList<>();
        List<CustomersBedHistory> customersBedHistoryList = new ArrayList<>();
        List<SettlementDetails> settlementDetailsList = new ArrayList<>();
        List<CustomerWalletHistory> cwhList = new ArrayList<>();
        List<SettlementItems> settlementItemsList = new ArrayList<>();
        List<Customers> walletUpdateCustomers = new ArrayList<>();

        Date today = new Date();

        for (InvoicesV1 invoice : invoices) {

            if (invoice == null){
                throw new BadRequestException(Utils.INVOICE_NOT_FOUND);
            }

            Customers customer = customersMap.getOrDefault(invoice.getCustomerId(), null);
            if (customer == null){
                throw new BadRequestException(Utils.NO_CUSTOMER_FOUND);
            }

            if (InvoiceType.SETTLEMENT.name().equals(invoice.getInvoiceType())) {
                List<String> cIds = invoice.getCancelledInvoices();
                if (cIds != null && !cIds.isEmpty()) {
                    for (String cancelledInvoiceId : cIds) {
                        InvoicesV1 cancelledInvoice = cancelledInvoiceMap.getOrDefault(cancelledInvoiceId, null);
                        if (cancelledInvoice == null){
                            throw new BadRequestException(Utils.INVOICE_NOT_FOUND);
                        }

                        cancelledInvoice.setCancelledDate(null);
                        cancelledInvoice.setCancelled(false);
                        cancelledInvoice.setUpdatedAt(today);

                        cancelledInvoicesList.add(cancelledInvoice);
                    }
                }

                customer.setCustomerBedStatus(CustomerBedStatus.BED_ASSIGNED.name());
                customer.setCurrentStatus(CustomerStatus.NOTICE.name());
                customer.setLastUpdatedAt(today);

                BookingsV1 booking = bookingsMap.getOrDefault(invoice.getCustomerId(), null);
                if (booking == null){
                    throw new BadRequestException(Utils.BOOKING_NOT_FOUND);
                }

                int bedId = booking.getBedId();

                booking.setSettlementGeneratedDate(null);
                booking.setCurrentStatus(BookingsStatus.NOTICE.name());
                booking.setUpdatedAt(today);

                Beds bed = bedsMap.getOrDefault(bedId, null);
                if (bed == null){
                    throw new BadRequestException(Utils.BED_NOT_FOUND);
                }

                if (BedStatus.OCCUPIED.name().equals(bed.getCurrentStatus())){
                    throw new BadRequestException(Utils.BED_IS_OCCUPIED);
                }

                bed.setFreeFrom(booking.getLeavingDate());
                bed.setStatus(BedStatus.OCCUPIED.name());
                bed.setCurrentStatus(BedStatus.OCCUPIED.name());
                bed.setUpdatedAt(today);

                CustomersBedHistory customersBedHistory = customersBedHistoryMap
                        .getOrDefault(customer.getCustomerId(), null);

                if (bedId != customersBedHistory.getBedId()){
                    throw new BadRequestException(Utils.BED_MISMATCH_BED_HISTORY);
                }

                customersBedHistory.setEndDate(null);

                SettlementDetails settlementDetail = settlementDetailsMap
                        .getOrDefault(invoice.getCustomerId(), null);

                if (settlementDetail != null){
                    settlementDetailsList.add(settlementDetail);
                }

                bookingsList.add(booking);
                bedsList.add(bed);
                customersBedHistoryList.add(customersBedHistory);
                customersList.add(customer);

                SettlementItems settlementItem = settlementItemsMap
                        .getOrDefault(invoice.getInvoiceId(), null);

                if (settlementItem != null){
                    settlementItemsList.add(settlementItem);

                    boolean updateCustomerWallet = false;

                    List<EBItems> ebItems = settlementItem.getEbItems();

                    if (ebItems != null && !ebItems.isEmpty()){
                        for (EBItems ebItem : ebItems) {
                            CustomerWalletHistory cwh = new CustomerWalletHistory();
                            cwh.setTransactionDate(ebItem.getToDate());
                            cwh.setAmount(ebItem.getTotalAmount());
                            cwh.setBillingStatus(WalletBillingStatus.INVOICE_NOT_GENERATED.name());
                            cwh.setCustomerId(invoice.getCustomerId());
                            cwh.setSourceId(String.valueOf(ebItem.getCustomerEBId()));
                            cwh.setSourceType(WalletSource.ELECTRICITY.name());
                            cwh.setTransactionType(WalletTransactionType.CREDIT.name());
                            cwh.setBillStartDate(ebItem.getFromDate());
                            cwh.setBillEndDate(ebItem.getToDate());
                            cwh.setCreatedAt(today);

                            cwhList.add(cwh);

                            updateCustomerWallet = true;
                        }
                    }

                    List<WalltetItems> walletItems = settlementItem.getWalltetItems();

                    if (walletItems != null && !walletItems.isEmpty()){
                        for (WalltetItems walletItem : walletItems) {
                            CustomerWalletHistory cwh = customerWalletHistoryMap
                                    .getOrDefault(walletItem.getWalletId(), null);

                            if (cwh != null){
                                cwh.setBillingStatus(WalletBillingStatus.INVOICE_NOT_GENERATED.name());
                                cwh.setInvoiceId(null);

                                cwhList.add(cwh);

                                updateCustomerWallet = true;
                            }
                        }
                    }

                    if (updateCustomerWallet){
                        walletUpdateCustomers.add(customer);
                    }
                }
            }
        }

        bookingsService.saveAll(bookingsList);
        bedsService.saveAll(bedsList);
        customerBedHistoryService.saveAll(customersBedHistoryList);
        customersService.saveAll(customersList);
        invoiceV1Repository.saveAll(cancelledInvoicesList);
        customerWalletHistoryService.saveAll(cwhList);

        Set<String> walletUpdateCustomerIds = walletUpdateCustomers.stream()
                .map(Customers::getCustomerId)
                .collect(Collectors.toSet());

        List<CustomerWalletHistory> updatedCwhList = customerWalletHistoryService
                .getAllInvoiceNotGeneratedWalletsByCustomerIds(walletUpdateCustomerIds);

        Map<String, List<CustomerWalletHistory>> updatedCwhMap = updatedCwhList.stream()
                .collect(Collectors.groupingBy(CustomerWalletHistory::getCustomerId));

        List<Customers> updatableCustomers = new ArrayList<>();
        List<CustomerWallet> updatableCustomerWallets = new ArrayList<>();

        for (Customers walletUpdateCustomer : walletUpdateCustomers) {
            List<CustomerWalletHistory> customerWalletHistoryList = updatedCwhMap
                    .getOrDefault(walletUpdateCustomer.getCustomerId(), null);

            CustomerWallet cw = walletUpdateCustomer.getWallet();

            if (cw == null){
                cw = new CustomerWallet();
                cw.setCustomers(walletUpdateCustomer);
                walletUpdateCustomer.setWallet(cw);
            }

            double amount = 0;
            Date transactionDate = today;

            if (customerWalletHistoryList != null && !customerWalletHistoryList.isEmpty()){
                amount = customerWalletHistoryList.stream()
                        .mapToDouble(CustomerWalletHistory::getAmount)
                        .sum();
                transactionDate = customerWalletHistoryList.stream()
                        .map(CustomerWalletHistory::getTransactionDate)
                        .filter(Objects::nonNull)
                        .max(Date::compareTo)
                        .orElse(today);
            }

            cw.setAmount(amount);
            cw.setTransactionDate(transactionDate);

            updatableCustomerWallets.add(cw);
            updatableCustomers.add(walletUpdateCustomer);
        }

        customerWalletService.saveAll(updatableCustomerWallets);
        customersService.saveAll(updatableCustomers);

        settlementDetailsService.deleteAll(settlementDetailsList);
        settlementItemsService.deleteAll(settlementItemsList);

        deleteInvoiceRelatedData(invoiceIds);

        paymentSummaryService.updatePaymentSummaryByInvoices(invoices);

        invoiceV1Repository.deleteAll(invoices);
    }

    private void deleteInvoiceRelatedData(Set<String> invoiceIds) {

        List<CustomerWalletHistory> cwhList = customerWalletHistoryService
                .getByInvoiceIds(invoiceIds);
        List<CreditDebitNotes> creditDebitNotes = creditDebitNotesService
                .getByInvoiceIds(invoiceIds);
        List<InvoiceDiscounts> invoiceDiscounts = invoiceDiscountsService
                .getByInvoiceIds(invoiceIds);
        List<TransactionV1> transactions = transactionV1Service
                .getByInvoiceIds(invoiceIds);
        List<InvoiceRedemption> invoiceRedemptions = invoiceRedemptionService
                .getInvoiceRedemptionByInvoiceIds(invoiceIds);

        if (!invoiceRedemptions.isEmpty()){
            invoiceRedemptionService.deleteInvoiceRedemptions(invoiceRedemptions);
        }

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

        invoiceRedemptionService.deleteAll(invoiceRedemptions);
        customerWalletHistoryService.deleteAll(cwhList);
        creditDebitNotesService.deleteAll(creditDebitNotes);
        invoiceDiscountsService.deleteAll(invoiceDiscounts);
        transactionV1Service.deleteAll(transactions);
        bankTransactionService.deleteAll(bankTransactions);
    }

    public List<InvoicesV1> getInvoicesByIds(Set<String> invoiceIds) {
        return invoiceV1Repository.findAllByInvoiceIdIn(invoiceIds);
    }

    public InvoicesV1 getInvoiceById(String invoiceId) {
        return invoiceV1Repository.findByInvoiceId(invoiceId);
    }

    public InvoicesV1 save(InvoicesV1 invoice) {
        return invoiceV1Repository.save(invoice);
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
                    Users updatedByUser = userMap.getOrDefault(invoice.getUpdatedBy(), null);
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

    public ResponseEntity<?> deleteInvoicesByIds(List<InvoiceIdMobilePayload> payloads) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Invoices.getId(), Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Set<String> invoiceIds = payloads.stream()
                .map(InvoiceIdMobilePayload::invoiceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<InvoicesV1> invoices = invoiceV1Repository.findAllByInvoiceIdIn(invoiceIds);

        Map<String, InvoicesV1> invoiceMap = invoices.stream()
                .collect(Collectors.toMap(InvoicesV1::getInvoiceId, invoice -> invoice));

        Set<String> cancelledInvoiceIds = invoices.stream()
                .filter(invoice -> InvoiceType.SETTLEMENT.name().equals(invoice.getInvoiceType()))
                .map(InvoicesV1::getCancelledInvoices)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        List<InvoicesV1> cancelledInvoices = invoiceV1Repository.findAllByInvoiceIdIn(cancelledInvoiceIds);

        Map<String, InvoicesV1> cancelledInvoiceMap = cancelledInvoices.stream()
                .collect(Collectors.toMap(InvoicesV1::getInvoiceId, invoice -> invoice));

        Set<String> customerIds = invoices.stream()
                .map(InvoicesV1::getCustomerId)
                .collect(Collectors.toSet());

        List<Customers> customers = customersService.getCustomersByIds(customerIds);

        Map<String, Customers> customersMap = customers.stream()
                .collect(Collectors.toMap(Customers::getCustomerId, customer -> customer));

        List<BookingsV1> bookings = bookingsService.getBookingsByCustomerIds(customerIds);

        Map<String, BookingsV1> bookingsMap = bookings.stream()
                .collect(Collectors.toMap(BookingsV1::getCustomerId, booking -> booking));

        Set<Integer> bedIds = bookings.stream()
                .map(BookingsV1::getBedId)
                .collect(Collectors.toSet());

        List<Beds> beds = bedsService.getBedsByBedIds(bedIds);

        Map<Integer, Beds> bedsMap = beds.stream()
                .collect(Collectors.toMap(Beds::getBedId, bed -> bed));

        List<SettlementDetails> settlementDetails = settlementDetailsService
                .findByCustomerIds(new ArrayList<>(customerIds));

        Map<String, SettlementDetails> settlementDetailsMap = settlementDetails.stream()
                .collect(Collectors.toMap(SettlementDetails::getCustomerId, sd -> sd));

        List<SettlementItems> settlementItems = settlementItemsService
                .getByInvoiceIds(invoiceIds);

        Map<String, SettlementItems> settlementItemsMap = settlementItems.stream()
                .collect(Collectors.toMap(SettlementItems::getInvoiceId, item -> item));

        Set<Long> cwhIds = settlementItems.stream()
                .map(SettlementItems::getWalltetItems)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(WalltetItems::getWalletId)
                .collect(Collectors.toSet());

        List<CustomerWalletHistory> customerWalletHistories = customerWalletHistoryService
                .getByCustomerWalletHistoryIds(cwhIds);

        Map<Long, CustomerWalletHistory> customerWalletHistoryMap = customerWalletHistories.stream()
                .collect(Collectors.toMap(CustomerWalletHistory::getHistoryId, cwh -> cwh));

        List<CustomersBedHistory> customersBedHistories = customerBedHistoryService
                .getLatestBedHistoriesByCustomerIds(customerIds);

        Map<String, CustomersBedHistory> customersBedHistoryMap = customersBedHistories.stream()
                .collect(Collectors.toMap(CustomersBedHistory::getCustomerId, cbh -> cbh));

        List<InvoicesV1> cancelledInvoicesList = new ArrayList<>();
        List<Customers> customersList = new ArrayList<>();
        List<BookingsV1> bookingsList = new ArrayList<>();
        List<Beds> bedsList = new ArrayList<>();
        List<CustomersBedHistory> customersBedHistoryList = new ArrayList<>();
        List<SettlementDetails> settlementDetailsList = new ArrayList<>();
        List<CustomerWalletHistory> cwhList = new ArrayList<>();
        List<SettlementItems> settlementItemsList = new ArrayList<>();
        List<Customers> walletUpdateCustomers = new ArrayList<>();

        Date today = new Date();

        for (InvoiceIdMobilePayload payload : payloads) {

            String invoiceId = payload.invoiceId();
            String tenantMobile = payload.tenantMobile();

            if (invoiceId == null || invoiceId.isBlank()){
                return new ResponseEntity<>(Utils.INVOICE_ID_REQUIRED, HttpStatus.BAD_REQUEST);
            }
            if (tenantMobile == null || tenantMobile.isBlank()){
                return new ResponseEntity<>(Utils.TENANT_MOBILE_REQUIRED, HttpStatus.BAD_REQUEST);
            }

            InvoicesV1 invoice = invoiceMap.getOrDefault(invoiceId, null);
            if (invoice == null){
                return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }

            Customers customer = customersMap.getOrDefault(invoice.getCustomerId(), null);
            if (customer == null){
                return new ResponseEntity<>(Utils.NO_CUSTOMER_FOUND, HttpStatus.BAD_REQUEST);
            }

            if (!customer.getMobile().equals(tenantMobile)){
                return new ResponseEntity<>(Utils.TENANT_MOBILE_MISMATCH, HttpStatus.BAD_REQUEST);
            }

            if (InvoiceType.SETTLEMENT.name().equals(invoice.getInvoiceType())) {
                List<String> cIds = invoice.getCancelledInvoices();
                if (cIds != null && !cIds.isEmpty()) {
                    for (String cancelledInvoiceId : cIds) {
                        InvoicesV1 cancelledInvoice = cancelledInvoiceMap.getOrDefault(cancelledInvoiceId, null);
                        if (cancelledInvoice == null){
                            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
                        }

                        cancelledInvoice.setCancelledDate(null);
                        cancelledInvoice.setCancelled(false);
                        cancelledInvoice.setUpdatedAt(today);

                        cancelledInvoicesList.add(cancelledInvoice);
                    }
                }

                customer.setCustomerBedStatus(CustomerBedStatus.BED_ASSIGNED.name());
                customer.setCurrentStatus(CustomerStatus.NOTICE.name());
                customer.setLastUpdatedAt(today);

                BookingsV1 booking = bookingsMap.getOrDefault(invoice.getCustomerId(), null);
                if (booking == null){
                    return new ResponseEntity<>(Utils.BOOKING_NOT_FOUND, HttpStatus.BAD_REQUEST);
                }

                int bedId = booking.getBedId();

                booking.setSettlementGeneratedDate(null);
                booking.setCurrentStatus(BookingsStatus.NOTICE.name());
                booking.setUpdatedAt(today);

                Beds bed = bedsMap.getOrDefault(bedId, null);
                if (bed == null){
                    return new ResponseEntity<>(Utils.BED_NOT_FOUND, HttpStatus.BAD_REQUEST);
                }

                if (BedStatus.OCCUPIED.name().equals(bed.getCurrentStatus())){
                    return new ResponseEntity<>(Utils.BED_IS_OCCUPIED, HttpStatus.BAD_REQUEST);
                }

                bed.setFreeFrom(booking.getLeavingDate());
                bed.setStatus(BedStatus.OCCUPIED.name());
                bed.setCurrentStatus(BedStatus.OCCUPIED.name());
                bed.setUpdatedAt(today);

                CustomersBedHistory customersBedHistory = customersBedHistoryMap
                        .getOrDefault(customer.getCustomerId(), null);

                if (bedId != customersBedHistory.getBedId()){
                    return new ResponseEntity<>(Utils.BED_MISMATCH_BED_HISTORY, HttpStatus.BAD_REQUEST);
                }

                customersBedHistory.setEndDate(null);

                SettlementDetails settlementDetail = settlementDetailsMap
                        .getOrDefault(invoice.getCustomerId(), null);

                if (settlementDetail != null){
                    settlementDetailsList.add(settlementDetail);
                }

                bookingsList.add(booking);
                bedsList.add(bed);
                customersBedHistoryList.add(customersBedHistory);
                customersList.add(customer);

                SettlementItems settlementItem = settlementItemsMap
                        .getOrDefault(invoice.getInvoiceId(), null);

                if (settlementItem != null){
                    settlementItemsList.add(settlementItem);

                    boolean updateCustomerWallet = false;

                    List<EBItems> ebItems = settlementItem.getEbItems();

                    if (ebItems != null && !ebItems.isEmpty()){
                        for (EBItems ebItem : ebItems) {
                            CustomerWalletHistory cwh = new CustomerWalletHistory();
                            cwh.setTransactionDate(ebItem.getToDate());
                            cwh.setAmount(ebItem.getTotalAmount());
                            cwh.setBillingStatus(WalletBillingStatus.INVOICE_NOT_GENERATED.name());
                            cwh.setCustomerId(invoice.getCustomerId());
                            cwh.setSourceId(String.valueOf(ebItem.getCustomerEBId()));
                            cwh.setSourceType(WalletSource.ELECTRICITY.name());
                            cwh.setTransactionType(WalletTransactionType.CREDIT.name());
                            cwh.setBillStartDate(ebItem.getFromDate());
                            cwh.setBillEndDate(ebItem.getToDate());
                            cwh.setCreatedAt(today);

                            cwhList.add(cwh);

                            updateCustomerWallet = true;
                        }
                    }

                    List<WalltetItems> walletItems = settlementItem.getWalltetItems();

                    if (walletItems != null && !walletItems.isEmpty()){
                        for (WalltetItems walletItem : walletItems) {
                            CustomerWalletHistory cwh = customerWalletHistoryMap
                                    .getOrDefault(walletItem.getWalletId(), null);

                            if (cwh != null){
                                cwh.setBillingStatus(WalletBillingStatus.INVOICE_NOT_GENERATED.name());
                                cwh.setInvoiceId(null);

                                cwhList.add(cwh);

                                updateCustomerWallet = true;
                            }
                        }
                    }

                    if (updateCustomerWallet){
                        walletUpdateCustomers.add(customer);
                    }
                }
            }
        }

        bookingsService.saveAll(bookingsList);
        bedsService.saveAll(bedsList);
        customerBedHistoryService.saveAll(customersBedHistoryList);
        customersService.saveAll(customersList);
        invoiceV1Repository.saveAll(cancelledInvoicesList);
        customerWalletHistoryService.saveAll(cwhList);

        Set<String> walletUpdateCustomerIds = walletUpdateCustomers.stream()
                .map(Customers::getCustomerId)
                .collect(Collectors.toSet());

        List<CustomerWalletHistory> updatedCwhList = customerWalletHistoryService
                .getAllInvoiceNotGeneratedWalletsByCustomerIds(walletUpdateCustomerIds);

        Map<String, List<CustomerWalletHistory>> updatedCwhMap = updatedCwhList.stream()
                .collect(Collectors.groupingBy(CustomerWalletHistory::getCustomerId));

        List<Customers> updatableCustomers = new ArrayList<>();
        List<CustomerWallet> updatableCustomerWallets = new ArrayList<>();

        for (Customers walletUpdateCustomer : walletUpdateCustomers) {
            List<CustomerWalletHistory> customerWalletHistoryList = updatedCwhMap
                    .getOrDefault(walletUpdateCustomer.getCustomerId(), null);

            CustomerWallet cw = walletUpdateCustomer.getWallet();

            if (cw == null){
                cw = new CustomerWallet();
                cw.setCustomers(walletUpdateCustomer);
                walletUpdateCustomer.setWallet(cw);
            }

            double amount = 0;
            Date transactionDate = today;

            if (customerWalletHistoryList != null && !customerWalletHistoryList.isEmpty()){
                amount = customerWalletHistoryList.stream()
                        .mapToDouble(CustomerWalletHistory::getAmount)
                        .sum();
                transactionDate = customerWalletHistoryList.stream()
                        .map(CustomerWalletHistory::getTransactionDate)
                        .filter(Objects::nonNull)
                        .max(Date::compareTo)
                        .orElse(today);
            }

            cw.setAmount(amount);
            cw.setTransactionDate(transactionDate);

            updatableCustomerWallets.add(cw);
            updatableCustomers.add(walletUpdateCustomer);
        }

        customerWalletService.saveAll(updatableCustomerWallets);
        customersService.saveAll(updatableCustomers);

        settlementDetailsService.deleteAll(settlementDetailsList);
        settlementItemsService.deleteAll(settlementItemsList);

        deleteInvoiceRelatedData(invoiceIds);

        paymentSummaryService.updatePaymentSummaryByInvoices(invoices);

        invoiceV1Repository.deleteAll(invoices);

        List<InvoiceSnapshot> invoiceSnapshotList = SnapshotUtility.toSnapshotList(invoices, SnapshotUtility::toSnapshot);

        InvoiceSnapshotWrapper invoiceSnapshotWrapper = new InvoiceSnapshotWrapper(invoiceSnapshotList);

        agentActivitiesService.createAgentActivity(agent, ActivityType.SNAPSHOT_DELETE, Source.INVOICE,
                null, invoiceSnapshotWrapper, null);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }

    public void saveAll(List<InvoicesV1> invoiceList) {
        invoiceV1Repository.saveAll(invoiceList);
    }

    public List<InvoicesV1> getInvoicesByInvoiceType(String invoiceType) {
        return invoiceV1Repository.findAllByInvoiceType(invoiceType);
    }

    public ResponseEntity<?> getReceiptsByInvoiceId(String hostelId, String invoiceId) {

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

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Receipt.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelService.getHostelByHostelId(hostelId);
        if (hostel == null) {
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 invoice = invoiceV1Repository.findByInvoiceId(invoiceId);
        if (invoice == null) {
            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!invoice.getHostelId().equals(hostelId)) {
            return new ResponseEntity<>(Utils.INVOICE_HOSTEL_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        if (!InvoiceType.ADVANCE.name().equals(invoice.getInvoiceType())) {
            return new ResponseEntity<>(Utils.INVOICE_IS_NOT_ADVANCE, HttpStatus.BAD_REQUEST);
        }

        if (!PaymentStatus.PAID.name().equals(invoice.getPaymentStatus())) {
            return new ResponseEntity<>(Utils.INVOICE_NOT_PAID, HttpStatus.BAD_REQUEST);
        }

        Customers customer = customersService.getCustomerInformation(invoice.getCustomerId());
        if (customer == null) {
            return new ResponseEntity<>(Utils.NO_CUSTOMER_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<TransactionV1> transactions = transactionV1Service.getByInvoiceId(invoiceId);

        Set<String> bankIds = transactions.stream()
                .map(TransactionV1::getBankId)
                .collect(Collectors.toSet());

        List<BankingV1> banks = bankingService.findByBankIds(bankIds);

        Map<String, BankingV1> bankMap = banks.stream()
                .collect(Collectors.toMap(BankingV1::getBankId, bank -> bank));

        Set<String> userIds = new HashSet<>();
        for (TransactionV1 transaction : transactions) {
            if (transaction.getCreatedBy() != null){
                userIds.add(transaction.getCreatedBy());
            }
            if (transaction.getUpdatedBy() != null){
                userIds.add(transaction.getUpdatedBy());
            }
        }

        List<Users> users = usersService.getUsersByIds(userIds);

        Map<String, Users> usersMap = users.stream()
                .collect(Collectors.toMap(Users::getUserId, user -> user));

        List<TransactionResponse> response = transactions.stream()
                .map(transaction -> {

                    BankingV1 bank = bankMap.getOrDefault(transaction.getBankId(), null);
                    Users createdByUser = usersMap.getOrDefault(transaction.getCreatedBy(), null);
                    Users updatedByUser = usersMap.getOrDefault(transaction.getUpdatedBy(), null);

                    return new TransactionResMapper(invoice, hostel, customer,
                            bank, createdByUser, updatedByUser)
                            .apply(transaction);
                }).toList();

        boolean canUpdateInvoiceBalance = false;
        if (InvoiceType.ADVANCE.name().equals(invoice.getInvoiceType()) &&
                PaymentStatus.PAID.name().equals(invoice.getPaymentStatus())){
            canUpdateInvoiceBalance = true;
        }

        TransactionResWrapper resWrapper = new TransactionResWrapper(canUpdateInvoiceBalance, response);

        return new ResponseEntity<>(resWrapper, HttpStatus.OK);
    }

    public ResponseEntity<?> updateAdvanceInvoiceBalance(String hostelId, String invoiceId,
                                                         AdvanceBalanceAmountPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Invoices.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelService.getHostelByHostelId(hostelId);
        if (hostel == null) {
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 invoice = invoiceV1Repository.findByInvoiceId(invoiceId);
        if (invoice == null) {
            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!invoice.getHostelId().equals(hostelId)) {
            return new ResponseEntity<>(Utils.INVOICE_HOSTEL_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        if (!InvoiceType.ADVANCE.name().equals(invoice.getInvoiceType())) {
            return new ResponseEntity<>(Utils.INVOICE_IS_NOT_ADVANCE, HttpStatus.BAD_REQUEST);
        }

        if (!PaymentStatus.PAID.name().equals(invoice.getPaymentStatus())) {
            return new ResponseEntity<>(Utils.INVOICE_NOT_PAID, HttpStatus.BAD_REQUEST);
        }

        InvoiceSnapshot oldInvoiceSnapshot = SnapshotUtility.toSnapshot(invoice);

        double payloadBalanceAmount = payload.balanceAmount();
        payloadBalanceAmount = Utils.roundOfDoubleTo2Digits(payloadBalanceAmount);

        double invoiceTotalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0;
        double invoiceBalanceAmount = invoice.getBalanceAmount() != null ? invoice.getBalanceAmount() : 0;
        double invoicePaidAmount = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : 0;

        List<Deductions> invoiceDeductions = invoice.getDeductions() != null ? invoice.getDeductions() : Collections.emptyList();

        double invoiceDeductionsAmount = invoiceDeductions.stream()
                .mapToDouble(Deductions::getAmount)
                .sum();

        List<InvoiceRedemption> invoiceRedemptions = invoiceRedemptionService
                .getInvoiceRedemptionBySourceInvoiceId(invoiceId);

        double invoiceRedemptionAmount = invoiceRedemptions.stream()
                .mapToDouble(InvoiceRedemption::getRedemptionAmount)
                .sum();

        double expectedBalanceAmount = invoicePaidAmount - invoiceDeductionsAmount - invoiceRedemptionAmount;
        expectedBalanceAmount = Utils.roundOfDoubleTo2Digits(expectedBalanceAmount);

        if (invoiceBalanceAmount == payloadBalanceAmount) {
            return new ResponseEntity<>(Utils.NO_CHANGES_DETECTED, HttpStatus.BAD_REQUEST);
        }

        if (payloadBalanceAmount != expectedBalanceAmount) {
            return new ResponseEntity<>("Invoice balance amount is : " + invoiceBalanceAmount +
                            "\nExpected balance amount is : " + expectedBalanceAmount +
                            "\nTotal amount is : " + invoiceTotalAmount +
                            "\nPaid amount is : " + invoicePaidAmount +
                            "\nDeductions amount is : " + invoiceDeductionsAmount +
                            "\nRedemption amount is : " + invoiceRedemptionAmount,
                    HttpStatus.BAD_REQUEST);
        }

        invoice.setBalanceAmount(payloadBalanceAmount);

        invoice = invoiceV1Repository.save(invoice);

        InvoiceSnapshot newInvoiceSnapshot = SnapshotUtility.toSnapshot(invoice);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.INVOICE,
                invoiceId, oldInvoiceSnapshot, newInvoiceSnapshot);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> updateAdvanceInvoiceAmount(String hostelId, String invoiceId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Invoices.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelService.getHostelByHostelId(hostelId);
        if (hostel == null) {
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 invoice = invoiceV1Repository.findByInvoiceId(invoiceId);
        if (invoice == null) {
            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!invoice.getHostelId().equals(hostelId)) {
            return new ResponseEntity<>(Utils.INVOICE_HOSTEL_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        if (!InvoiceType.ADVANCE.name().equals(invoice.getInvoiceType())) {
            return new ResponseEntity<>(Utils.INVOICE_IS_NOT_ADVANCE, HttpStatus.BAD_REQUEST);
        }

        Customers customer = customersService.getCustomerInformation(invoice.getCustomerId());
        if (customer == null) {
            return new ResponseEntity<>(Utils.NO_CUSTOMER_FOUND, HttpStatus.BAD_REQUEST);
        }

        InvoiceSnapshot oldInvoiceSnapshot = SnapshotUtility.toSnapshot(invoice);

        Advance advance = customer.getAdvance();
        if (advance == null) {
            return new ResponseEntity<>(Utils.ADVANCE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        double advanceAmount = advance.getAdvanceAmount();
        double paidAmount = advance.getPaidAmount();

        double invoiceDeductionsAmount = invoice.getDeductionAmount() != null ? invoice.getDeductionAmount() : 0;

        double invoiceBasePrice = advanceAmount;
        double invoiceTotalAmount = advanceAmount + invoiceDeductionsAmount;
        double invoiceSubTotal = advanceAmount + invoiceDeductionsAmount;
        double invoicePaidAmount = paidAmount;

        List<InvoiceRedemption> invoiceRedemptions = invoiceRedemptionService
                .getInvoiceRedemptionBySourceInvoiceId(invoiceId);

        double invoiceRedemptionAmount = invoiceRedemptions.stream()
                .mapToDouble(r -> r.getRedemptionAmount() != null ? r.getRedemptionAmount() : 0)
                .sum();

        double expectedBalanceAmount = invoicePaidAmount - invoiceDeductionsAmount - invoiceRedemptionAmount;
        expectedBalanceAmount = Utils.roundOfDoubleTo2Digits(expectedBalanceAmount);

        if (expectedBalanceAmount < 0){
            expectedBalanceAmount = 0;
        }

        if (Objects.equals(invoicePaidAmount, invoiceTotalAmount)){
            invoice.setPaymentStatus(PaymentStatus.PAID.name());
        } else if (invoicePaidAmount <= 0) {
            invoice.setPaymentStatus(PaymentStatus.PENDING.name());
        } else if (invoicePaidAmount > 0 && invoicePaidAmount < invoiceTotalAmount) {
            invoice.setPaymentStatus(PaymentStatus.PARTIAL_PAYMENT.name());
        }

        invoice.setTotalAmount(invoiceTotalAmount);
        invoice.setBasePrice(invoiceBasePrice);
        invoice.setSubTotal(invoiceSubTotal);
        invoice.setBalanceAmount(expectedBalanceAmount);

        invoice = invoiceV1Repository.save(invoice);

        InvoiceSnapshot newInvoiceSnapshot = SnapshotUtility.toSnapshot(invoice);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.INVOICE,
                invoiceId, oldInvoiceSnapshot, newInvoiceSnapshot);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public List<InvoicesV1> getInvoicesByCustomerIdAndInvoiceType(String customerId, String invoiceType){
        return invoiceV1Repository.findByCustomerIdAndInvoiceType(customerId, invoiceType);
    }

    public List<InvoicesV1> getOlderUnpaidInvoicesByInvoiceTypes(String customerId, Set<String> invoiceTypes,
                                                                 Date beforeDate){
        String paidName = PaymentStatus.PAID.name();

        return invoiceV1Repository.findOlderUnpaidInvoicesByInvoiceTypes(customerId, invoiceTypes,
                beforeDate, paidName);
    }

    public List<InvoicesV1> getCurrentMonthInvoices(String customerId, String hostelId, Date startDate) {
        return invoiceV1Repository.findAllCurrentMonthInvoices(customerId, hostelId, startDate);
    }

    public List<InvoicesV1> getInvoicesByCustomerIdAndStartDateAfter(String customerId, Date afterDate) {
        return invoiceV1Repository.findInvoicesByCustomerIdAndStartDateAfter(customerId, afterDate);
    }

    public String generateInvoiceNumber(String hostelId, String type) {

        StringBuilder invoiceNumber = new StringBuilder();

        BillTemplatesDto templates = templatesService.getBillTemplate(hostelId, type);

        InvoicesV1 existing = null;

        if (templates != null) {
            existing = invoiceV1Repository
                    .findLatestInvoiceByPrefix(templates.prefix(), hostelId);

            invoiceNumber = new StringBuilder();
            invoiceNumber.append(templates.prefix());

            if (existing == null) {
                invoiceNumber.append("-001");
            } else {
                String[] suffix = existing.getInvoiceNumber().split("-");
                if (suffix.length > 1) {
                    invoiceNumber.append("-");
                    int suff = Integer.parseInt(suffix[suffix.length - 1]) + 1;
                    invoiceNumber.append(String.format("%03d", suff));
                }
            }
        } else {
            invoiceNumber.append("INV");
            invoiceNumber.append("-");
            invoiceNumber.append("001");
        }

        return invoiceNumber.toString();
    }
}
