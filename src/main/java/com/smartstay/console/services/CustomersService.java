package com.smartstay.console.services;

import com.smartstay.console.Mapper.customers.CustomerSumMapper;
import com.smartstay.console.Mapper.invoice.InvoiceResponseMapper;
import com.smartstay.console.Mapper.invoiceRedemption.InvoiceRedemptionResMapper;
import com.smartstay.console.Mapper.transaction.TransactionResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.customers.CustomerResetSnapshot;
import com.smartstay.console.dto.customers.CustomersCredentialsSnapshot;
import com.smartstay.console.dto.customers.CustomersSnapshot;
import com.smartstay.console.dto.customers.Deductions;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.dto.settlementDetails.SettlementDetailsSnapshot;
import com.smartstay.console.ennum.*;
import com.smartstay.console.payloads.customers.CustomerDatePayload;
import com.smartstay.console.payloads.customers.CustomerResetPayload;
import com.smartstay.console.repositories.CustomersRepository;
import com.smartstay.console.responses.customers.*;
import com.smartstay.console.responses.invoice.InvoiceResponse;
import com.smartstay.console.responses.invoiceRedemption.InvoiceRedemptionRes;
import com.smartstay.console.responses.transaction.TransactionResponse;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CustomersService {

    @Autowired
    private CustomersRepository customersRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private PaymentSummaryService paymentSummaryService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private InvoiceV1Service invoiceV1Service;
    @Autowired
    private CustomerConfigService customersConfigService;
    @Autowired
    private CustomerDocumentService customerDocumentService;
    @Autowired
    private AmenityRequestService amenityRequestService;
    @Autowired
    private ComplaintService complaintService;
    @Autowired
    private CreditDebitNotesService creditDebitNotesService;
    @Autowired
    private CustomersAmenityService customersAmenityService;
    @Autowired
    private CustomerBedHistoryService customerBedHistoryService;
    @Autowired
    private CustomerEbHistoryService customerEbHistoryService;
    @Autowired
    private CustomerWalletHistoryService customerWalletHistoryService;
    @Autowired
    private TransactionV1Service transactionV1Service;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private BankTransactionService bankTransactionService;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private CustomerAdditionalContactsService customerAdditionalContactsService;
    @Autowired
    private CustomersCredentialService customersCredentialService;
    @Autowired
    private CustomersOtpService customersOtpService;
    @Autowired
    private BedChangeRequestService bedChangeRequestService;
    @Autowired
    private CustomerBillingRulesService customerBillingRulesService;
    @Autowired
    private CustomerRecurringTrackerService customerRecurringTrackerService;
    @Autowired
    private InvoiceDiscountsService invoiceDiscountsService;
    @Autowired
    private RentHistoryService rentHistoryService;
    @Autowired
    private SettlementDetailsService settlementDetailsService;
    @Autowired
    private CustomerNotificationsService customerNotificationsService;
    @Autowired
    private SettlementItemsService settlementItemsService;
    @Autowired
    private InvoiceRedemptionService invoiceRedemptionService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private FloorsService floorsService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private BedsService bedService;
    @Autowired
    private BillingRulesService billingRulesService;
    @Autowired
    private ElectricityReadingsService electricityReadingsService;

    public List<Customers> getCustomersByIds(Set<String> customerIds) {
        return customersRepository.findAllByCustomerIdIn(customerIds);
    }

    public ResponseEntity<?> getTenantsWithPaymentSummary(int page, int size, String tenantName) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Tenant_Summary.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);
        Pageable pageable = PageRequest.of(page, size);

        Page<Customers> pagedTenants;

        if (tenantName != null && !tenantName.isBlank()){
            pagedTenants = customersRepository
                    .findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrderByCreatedAtDesc(
                            tenantName, tenantName, pageable);
        } else {
            pagedTenants = customersRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<Customers> tenants = pagedTenants.getContent();

        if (tenants.isEmpty()){
            Map<String, Object> emptyResponse = Map.of(
                    "content", List.of(),
                    "currentPage", page + 1,
                    "pageSize", size,
                    "totalItems", 0,
                    "totalPages", 0
            );

            return new ResponseEntity<>(emptyResponse, HttpStatus.OK);
        }

        Set<String> hostelIds = tenants.stream()
                .map(Customers::getHostelId)
                .collect(Collectors.toSet());

        List<HostelV1> hostels = hostelService.getHostelsByHostelIds(hostelIds);

        Map<String, HostelV1> hostelMap = hostels.stream()
                .collect(Collectors.toMap(HostelV1::getHostelId,
                        hostel -> hostel));

        Set<String> customerIds = tenants.stream()
                .map(Customers::getCustomerId)
                .collect(Collectors.toSet());

        List<PaymentSummary> paymentSummaries = paymentSummaryService
                .getSummaryByCustomerIds(customerIds);

        Map<String, PaymentSummary> paymentSummaryMap = paymentSummaries.stream()
                .collect(Collectors.toMap(PaymentSummary::getCustomerId,
                        paymentSummary -> paymentSummary));

        List<CustomerSummaryResponse> tenantSummaries = tenants.stream()
                .map(tenant -> new CustomerSumMapper(
                        hostelMap.getOrDefault(tenant.getHostelId(), null),
                        paymentSummaryMap.getOrDefault(tenant.getCustomerId(), null)
                ).apply(tenant)).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content", tenantSummaries);
        response.put("currentPage", page+1);
        response.put("pageSize", size);
        response.put("totalItems", pagedTenants.getTotalElements());
        response.put("totalPages", pagedTenants.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public List<Customers> findCustomersByHostelId(String hostelId) {
        List<Customers> listCustomers = customersRepository.findByHostelId(hostelId);
        if (listCustomers == null) {
            listCustomers = new ArrayList<>();
        }

        return listCustomers;
    }

    public void deleteAll(List<Customers> customersList) {
        customersRepository.deleteAll(customersList);
    }

    public ResponseEntity<?> deleteTenant(String hostelId, String customerId,
                                          CustomerResetPayload customerResetPayload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Tenants.getId(), Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Customers customer = customersRepository.findByCustomerIdAndHostelId(customerId, hostelId);
        if (customer == null){
            return new ResponseEntity<>(Utils.NO_TENANT_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!customer.getMobile().equals(customerResetPayload.tenantMobile())){
            return new ResponseEntity<>(Utils.TENANT_MOBILE_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        CustomerCredentials customerCredentials = null;
        CustomersOtp customersOtp = null;
        boolean isDuplicate = customersRepository.existsByXuidAndCustomerIdNot(customer.getXuid(), customerId);
        if (!isDuplicate) {
            customerCredentials = customersCredentialService.findByXuid(customer.getXuid());
            customersOtp = customersOtpService.findByXuid(customer.getXuid());
        }

        List<InvoicesV1> invoicesList = invoiceV1Service.findAllByHostelIdAndCustomerId(hostelId, customerId);
        List<BookingsV1> listBookings = bookingsService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<CustomersConfig> listConfigs = customersConfigService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<CustomerDocuments> listCustomerDocuments = customerDocumentService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<CustomerAdditionalContacts> listCustomerAdditionalContacts = customerAdditionalContactsService
                .findByHostelIdAndCustomerId(hostelId, customerId);
        List<AmenityRequest> listAmenityRequests = amenityRequestService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<BedChangeRequest> listBedChangeRequests = bedChangeRequestService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<CustomerNotifications> listCustomerNotifications = customerNotificationsService.getByUserIds(Set.of(customerId));
        List<CustomerBillingRules> listCustomerBillingRules = customerBillingRulesService
                .findByHostelIdAndCustomerId(hostelId, customerId);
        List<CustomerRecurringTracker> listCustomerRecurringTrackers = customerRecurringTrackerService
                .findByHostelIdAndCustomerId(hostelId, customerId);
        List<InvoiceDiscounts> listInvoiceDiscounts = invoiceDiscountsService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<PaymentSummary> listPaymentSummary = paymentSummaryService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<RentHistory> listRentHistory = rentHistoryService.findByCustomerId(customerId);
        List<SettlementDetails> listSettlementDetails = settlementDetailsService.findByCustomerId(customerId);
        List<SettlementItems> listSettlementItems = settlementItemsService.findByCustomerId(customerId);
        List<ComplaintsV1> complaints = complaintService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<CreditDebitNotes> listCreditDebits = creditDebitNotesService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<CustomersAmenity> listCustomersAmenity = customersAmenityService.findByCustomerId(customerId);
        List<CustomersBedHistory> listCustomerBedHistory = customerBedHistoryService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<CustomersEbHistory> listCustomerEbHistory = customerEbHistoryService.findByCustomerId(customerId);
        List<CustomerWalletHistory> listCustomersWallet = customerWalletHistoryService.findByCustomerId(customerId);
        List<TransactionV1> listTransactions = transactionV1Service.findByHostelIdAndCustomerId(hostelId, customerId);
        Set<String> transactionIds = listTransactions.stream()
                .map(TransactionV1::getTransactionId)
                .collect(Collectors.toSet());
        Set<String> bankIds = listTransactions.stream()
                .map(TransactionV1::getBankId)
                .collect(Collectors.toSet());
        List<BankTransactionsV1> listBankTransactions = bankTransactionService.getTransactionsByTransactionIds(transactionIds);
        List<BankingV1> bankingList = bankingService.findByBankIds(bankIds);

        Set<String> activeStatuses = Set.of(
                BookingsStatus.CHECKIN.name(),
                BookingsStatus.BOOKED.name(),
                BookingsStatus.NOTICE.name()
        );

        Set<Integer> occupiedBedIds = listBookings.stream()
                .filter(b -> activeStatuses.contains(b.getCurrentStatus()))
                .map(BookingsV1::getBedId)
                .collect(Collectors.toSet());

        List<Beds> listBeds = bedsService.getBedsByBedIds(occupiedBedIds);

        List<BankTransactionsV1> listItemsOtherThanExpense = listBankTransactions
                .stream()
                .filter(i -> !i.getSource().equalsIgnoreCase(BankSource.EXPENSE.name()))
                .toList();

        HashMap<String, Double> bankBalances = new HashMap<>();

        CustomersSnapshot oldCustomer = SnapshotUtility.toSnapshot(customer);
        CustomersCredentialsSnapshot oldCredentials = SnapshotUtility.toSnapshot(customerCredentials);

        CustomerResetSnapshot snapshot = new CustomerResetSnapshot(
                oldCustomer,
                oldCredentials
        );

        if (invoicesList != null && !invoicesList.isEmpty()) {
            invoiceV1Service.deleteAllInvoices(invoicesList);

            Set<String> invoiceIds = invoicesList.stream()
                    .map(InvoicesV1::getInvoiceId)
                    .collect(Collectors.toSet());

            List<InvoiceRedemption> invoiceRedemptions = invoiceRedemptionService
                    .getInvoiceRedemptionByInvoiceIds(invoiceIds);

            if (!invoiceRedemptions.isEmpty()){
                invoiceRedemptionService.deleteAll(invoiceRedemptions);
            }
        }
        if (!listBookings.isEmpty()) {
            bookingsService.deleteBookings(listBookings);
        }
        if (listConfigs != null && !listConfigs.isEmpty()) {
            customersConfigService.deleteAll(listConfigs);
        }
        if (customerCredentials != null) {
            customersCredentialService.deleteCredential(customerCredentials);
        }
        if (customersOtp != null) {
            customersOtpService.delete(customersOtp);
        }
        if (listBedChangeRequests != null && !listBedChangeRequests.isEmpty()) {
            bedChangeRequestService.deleteAll(listBedChangeRequests);
        }
        if (listCustomerNotifications != null && !listCustomerNotifications.isEmpty()) {
            customerNotificationsService.deleteAll(listCustomerNotifications);
        }
        if (listCustomerBillingRules != null && !listCustomerBillingRules.isEmpty()) {
            customerBillingRulesService.deleteAll(listCustomerBillingRules);
        }
        if (listCustomerRecurringTrackers != null && !listCustomerRecurringTrackers.isEmpty()) {
            customerRecurringTrackerService.deleteAll(listCustomerRecurringTrackers);
        }
        if (listInvoiceDiscounts != null && !listInvoiceDiscounts.isEmpty()) {
            invoiceDiscountsService.deleteAll(listInvoiceDiscounts);
        }
        if (listPaymentSummary != null && !listPaymentSummary.isEmpty()) {
            paymentSummaryService.deleteAll(listPaymentSummary);
        }
        if (listRentHistory != null && !listRentHistory.isEmpty()) {
            rentHistoryService.deleteAll(listRentHistory);
        }
        if (listSettlementDetails != null && !listSettlementDetails.isEmpty()) {
            settlementDetailsService.deleteAll(listSettlementDetails);
        }
        if (listSettlementItems != null && !listSettlementItems.isEmpty()) {
            settlementItemsService.deleteAll(listSettlementItems);
        }
        if (listCustomerDocuments != null && !listCustomerDocuments.isEmpty()) {
            customerDocumentService.deleteDocuments(listCustomerDocuments);
        }
        if (listCustomerAdditionalContacts != null && !listCustomerAdditionalContacts.isEmpty()) {
            customerAdditionalContactsService.deleteAll(listCustomerAdditionalContacts);
        }
        if (listAmenityRequests != null && !listAmenityRequests.isEmpty()) {
            amenityRequestService.deleteAmenities(listAmenityRequests);
        }
        if (complaints != null && !complaints.isEmpty()) {
            complaintService.deleteAll(complaints);
        }
        if (listCreditDebits != null && !listCreditDebits.isEmpty()) {
            creditDebitNotesService.deleteAll(listCreditDebits);
        }
        if (listCustomersAmenity != null && !listCustomersAmenity.isEmpty()) {
            customersAmenityService.deleteAll(listCustomersAmenity);
        }
        if (listCustomerBedHistory != null && !listCustomerBedHistory.isEmpty()) {
            customerBedHistoryService.deleteAll(listCustomerBedHistory);
        }
        if (listCustomerEbHistory != null && !listCustomerEbHistory.isEmpty()) {
            customerEbHistoryService.deleteAll(listCustomerEbHistory);
        }
        if (listCustomersWallet != null && !listCustomersWallet.isEmpty()) {
            customerWalletHistoryService.deleteAll(listCustomersWallet);
        }
        if (!listTransactions.isEmpty()) {
            listTransactions.forEach(item -> {
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
            transactionV1Service.deleteAll(listTransactions);
        }
        if (!listItemsOtherThanExpense.isEmpty()) {
            bankTransactionService.deleteItemsOtherThanExpense(listItemsOtherThanExpense);
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
        if (listBeds != null && !listBeds.isEmpty()) {
            bedsService.makeAllBedAvailable(listBeds);
        }

        customersRepository.delete(customer);

        agentActivitiesService.createAgentActivity(agent, ActivityType.DELETE, Source.TENANT,
                customerId, snapshot, null);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public List<Customers> getCustomerDetails(List<String> customerIds) {
        List<Customers> listCustomers = new ArrayList<>();
        if (!customerIds.isEmpty()) {
            listCustomers = customersRepository.findByCustomerIdIn(customerIds);
        }
        return listCustomers;
    }

    public void updateCustomersFromRecurring(Customers customers) {
        customersRepository.save(customers);
    }

    public List<Customers> getCustomersByName(String name){
        return customersRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
    }

    public List<Customers> getCustomersByDays(Set<Integer> daySet){
        return customersRepository.findByDaySet(daySet);
    }

    public Page<Customers> getPaginatedCustomersByIds(Set<String> customerIds, Pageable pageable) {
        return customersRepository.findAllByCustomerIdInOrderByJoiningDateDesc(customerIds, pageable);
    }

    public Customers getCustomerInformation(String customerId) {
        return customersRepository.findById(customerId).orElse(null);
    }

    public Set<String> findConflictingXuids(List<String> xuids, List<String> customerIds) {
        return customersRepository.findConflictingXuids(xuids, customerIds);
    }

    public void saveAll(List<Customers> customersList) {
        customersRepository.saveAll(customersList);
    }

    public ResponseEntity<?> getCustomerDeductions(String hostelId, String customerId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Tenants.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Customers customer = customersRepository.findByCustomerIdAndHostelId(customerId, hostelId);
        if (customer == null){
            return new ResponseEntity<>(Utils.NO_TENANT_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        HostelV1 hostel = hostelService.getHostelByHostelId(hostelId);
        if (hostel == null) {
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        Advance advance = customer.getAdvance();

        List<Deductions> customerAdvanceDeductions = new ArrayList<>();

        if (advance != null) {
            customerAdvanceDeductions = advance.getDeductions();
        }

        List<DeductionsRes> customerAdvanceDeductionsRes = new ArrayList<>();
        if (customerAdvanceDeductions != null && !customerAdvanceDeductions.isEmpty()) {
            customerAdvanceDeductionsRes = customerAdvanceDeductions.stream()
                    .map(deduction -> new DeductionsRes(deduction.getType(),
                            deduction.getAmount(), deduction.getPaidAmount()))
                    .toList();
        }

        List<InvoicesV1> invoices = invoiceV1Service.findAllByHostelIdAndCustomerId(hostelId, customerId);

        List<InvoicesV1> advanceInvoices = invoices.stream()
                .filter(invoice -> InvoiceType.ADVANCE.name().equals(invoice.getInvoiceType()))
                .toList();

        List<InvoiceDeductionsRes> invoiceDeductionsRes = new ArrayList<>();
        if (!advanceInvoices.isEmpty()) {
            invoiceDeductionsRes = advanceInvoices.stream()
                    .map(advInvoice -> {
                        List<Deductions> advInvDeductions = advInvoice.getDeductions();
                        List<DeductionsRes> invoiceAdvanceDeductionsRes = new ArrayList<>();
                        if (advInvDeductions != null && !advInvDeductions.isEmpty()) {
                            invoiceAdvanceDeductionsRes = advInvDeductions.stream()
                                    .map(deduction -> new DeductionsRes(deduction.getType(),
                                            deduction.getAmount(), deduction.getPaidAmount()))
                                    .toList();
                        }
                        return new InvoiceDeductionsRes(advInvoice.getInvoiceId(), advInvoice.getInvoiceNumber(),
                                advInvoice.getInvoiceType(), invoiceAdvanceDeductionsRes);
                    }).toList();
        }

        CustomerDeductionsRes response = new CustomerDeductionsRes(customerId, hostelId,
                hostel.getHostelName(), Utils.getFullName(customer.getFirstName(), customer.getLastName()),
                customer.getCustomerBedStatus(), customer.getCurrentStatus(),
                customerAdvanceDeductionsRes, invoiceDeductionsRes);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> updateDeductions(String hostelId, String customerId, String invoiceId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Tenants.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Customers customer = customersRepository.findByCustomerIdAndHostelId(customerId, hostelId);
        if (customer == null){
            return new ResponseEntity<>(Utils.NO_TENANT_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        HostelV1 hostel = hostelService.getHostelByHostelId(hostelId);
        if (hostel == null) {
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 invoice = invoiceV1Service.getInvoiceById(invoiceId);
        if (invoice == null) {
            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (!invoice.getCustomerId().equals(customerId) || !invoice.getHostelId().equals(hostelId)) {
            return new ResponseEntity<>(Utils.INVOICE_HOSTEL_CUSTOMER_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        Advance advance = customer.getAdvance();
        if (advance == null || advance.getDeductions() == null || advance.getDeductions().isEmpty()) {
            return new ResponseEntity<>(Utils.NO_ADVANCE_DEDUCTIONS_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<Deductions> invDeductions = invoice.getDeductions();
        if (invDeductions == null) {
            invDeductions = new ArrayList<>();
        }

        if (!invDeductions.isEmpty()) {
            return new ResponseEntity<>(Utils.INVOICE_HAS_DEDUCTIONS_ALREADY, HttpStatus.BAD_REQUEST);
        }

        double remainingPaidAmount = invoice.getPaidAmount() == null
                ? 0
                : invoice.getPaidAmount();

        List<Deductions> copiedDeductions = new ArrayList<>();

        for (Deductions deduction : advance.getDeductions()) {

            double deductionAmount = deduction.getAmount();
            double paidAmountForDeduction = 0;

            if (remainingPaidAmount > 0) {
                paidAmountForDeduction = Math.min(remainingPaidAmount, deductionAmount);
                remainingPaidAmount -= paidAmountForDeduction;
            }

            copiedDeductions.add(
                    new Deductions(
                            deduction.getType(),
                            deductionAmount,
                            paidAmountForDeduction
                    )
            );
        }

        double deductionAmount = copiedDeductions.stream()
                        .mapToDouble(Deductions::getAmount)
                        .sum();

        invoice.setDeductionAmount(deductionAmount);
        invoice.setDeductions(copiedDeductions);

        invoiceV1Service.save(invoice);

        return new ResponseEntity<>(Utils.DEDUCTIONS_COPIED_SUCCESSFULLY , HttpStatus.OK);
    }

    public ResponseEntity<?> getCustomersWithPendingAdvanceDeductions() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Tenants.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<Customers> customers = customersRepository.findAll();

        List<InvoicesV1> advanceInvoices = invoiceV1Service
                .getInvoicesByInvoiceType(InvoiceType.ADVANCE.name());

        Map<String, List<InvoicesV1>> advanceInvoiceMap = advanceInvoices.stream()
                .filter(invoice -> invoice.getCustomerId() != null)
                .collect(Collectors.groupingBy(InvoicesV1::getCustomerId));

        Map<String, HostelV1> hostelMap = hostelService.getAllHostels()
                .stream()
                .collect(Collectors.toMap(
                        HostelV1::getHostelId,
                        Function.identity()
                ));

        List<CustomerDeductionsRes> response = customers.stream()

                // Customer must have advance deductions
                .filter(customer -> {
                    Advance advance = customer.getAdvance();

                    return advance != null
                            && advance.getDeductions() != null
                            && !advance.getDeductions().isEmpty();
                })

                // Customer must have an ADVANCE invoice
                .filter(customer -> advanceInvoiceMap.containsKey(customer.getCustomerId()))

                // ADVANCE invoice must have no deductions
                .filter(customer -> {
                    List<InvoicesV1> invoices = advanceInvoiceMap.get(customer.getCustomerId());

                    return invoices.stream()
                            .anyMatch(invoice ->
                                    invoice.getDeductions() == null
                                            || invoice.getDeductions().isEmpty());
                })

                .map(customer -> {

                    HostelV1 hostel = hostelMap.get(customer.getHostelId());

                    List<DeductionsRes> deductions = customer.getAdvance()
                                    .getDeductions()
                                    .stream()
                                    .map(d -> new DeductionsRes(
                                            d.getType(),
                                            d.getAmount(),
                                            d.getPaidAmount()))
                                    .toList();

                    List<InvoiceDeductionsRes> invoiceDeductionsRes =
                            advanceInvoiceMap
                                    .getOrDefault(customer.getCustomerId(), Collections.emptyList())
                                    .stream()
                                    .map(invoice -> {

                                        List<DeductionsRes> deductionRes =
                                                Optional.ofNullable(invoice.getDeductions())
                                                        .orElse(Collections.emptyList())
                                                        .stream()
                                                        .map(d -> new DeductionsRes(
                                                                d.getType(),
                                                                d.getAmount(),
                                                                d.getPaidAmount()))
                                                        .toList();

                                        return new InvoiceDeductionsRes(invoice.getInvoiceId(),
                                                invoice.getInvoiceNumber(), invoice.getInvoiceType(),
                                                deductionRes);
                                    }).toList();

                    return new CustomerDeductionsRes(customer.getCustomerId(), customer.getHostelId(),
                            hostel.getHostelName(), Utils.getFullName(customer.getFirstName(), customer.getLastName()),
                            customer.getCustomerBedStatus(), customer.getCurrentStatus(), deductions,
                            invoiceDeductionsRes
                    );
                }).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getTenantDetails(String customerId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Tenants.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Customers customer = customersRepository.findByCustomerId(customerId);
        if (customer == null){
            return new ResponseEntity<>(Utils.NO_CUSTOMER_FOUND, HttpStatus.BAD_REQUEST);
        }

        HostelV1 hostel = hostelService.getHostelByHostelId(customer.getHostelId());
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        BookingsV1 booking = bookingsService.getBookingInfoByCustomerId(customerId);
        if (booking == null){
            return new ResponseEntity<>(Utils.BOOKING_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Floors floor = floorsService.getByFloorId(booking.getFloorId());
        Rooms room = roomsService.getRoomById(booking.getRoomId());
        Beds bed = bedService.getBedById(booking.getBedId());

        List<InvoicesV1> invoices = invoiceV1Service
                .findAllByHostelIdAndCustomerId(customer.getHostelId(), customerId);

        Map<String, InvoicesV1> invoiceMap = invoices.stream()
                .collect(Collectors.toMap(InvoicesV1::getInvoiceId, Function.identity()));

        Set<String> invoiceIds = invoices.stream()
                .map(InvoicesV1::getInvoiceId)
                .collect(Collectors.toSet());

        List<TransactionV1> transactions = transactionV1Service
                .getByInvoiceIds(invoiceIds);

        List<InvoiceRedemption> invoiceRedemptions = invoiceRedemptionService
                .getInvoiceRedemptionByInvoiceIds(invoiceIds);

        Set<String> userIds = new HashSet<>();
        Set<String> bankIds = new HashSet<>();
        Set<String> agentIds = new HashSet<>();

        userIds.add(customer.getCreatedBy());
        userIds.add(customer.getUpdatedBy());

        for (InvoicesV1 invoice : invoices) {
            userIds.add(invoice.getCreatedBy());
            userIds.add(invoice.getUpdatedBy());
        }

        for (TransactionV1 transaction : transactions) {
            bankIds.add(transaction.getBankId());
            userIds.add(transaction.getCreatedBy());
            userIds.add(transaction.getUpdatedBy());
        }

        for (InvoiceRedemption redemption : invoiceRedemptions) {
            userIds.add(redemption.getCreatedBy());
            if (UserType.OWNER.name().equals(redemption.getUserType())) {
                userIds.add(redemption.getUpdatedBy());
            } else if (UserType.AGENT.name().equals(redemption.getUserType())) {
                agentIds.add(redemption.getUpdatedBy());
            }
        }

        List<Users> users = usersService.getUsersByIds(userIds);
        Map<String, Users> usersMap = users.stream()
                .collect(Collectors.toMap(Users::getUserId, user -> user));

        List<BankingV1> banks = bankingService.findByBankIds(bankIds);
        Map<String, BankingV1> bankMap = banks.stream()
                .collect(Collectors.toMap(BankingV1::getBankId, Function.identity()));

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, Function.identity()));

        String createdBy = null;
        String updatedBy = null;
        if (customer.getCreatedBy() != null) {
            if (usersMap.get(customer.getCreatedBy()) != null) {
                Users user = usersMap.get(customer.getCreatedBy());
                createdBy = Utils.getFullName(user.getFirstName(), user.getLastName());
            }
        }
        if (customer.getUpdatedBy() != null) {
            if (usersMap.get(customer.getUpdatedBy()) != null) {
                Users user = usersMap.get(customer.getUpdatedBy());
                updatedBy = Utils.getFullName(user.getFirstName(), user.getLastName());
            }
        }

        String createdAtDate = null;
        String createdAtTime = null;
        String updatedAtDate = null;
        String updatedAtTime = null;

        if (customer.getCreatedAt() != null) {
            createdAtDate = Utils.dateToString(customer.getCreatedAt());
            createdAtTime = Utils.dateToTime(customer.getCreatedAt());
        }
        if (customer.getLastUpdatedAt() != null) {
            updatedAtDate = Utils.dateToString(customer.getLastUpdatedAt());
            updatedAtTime = Utils.dateToTime(customer.getLastUpdatedAt());
        }

        TenantHostelDetailsRes hostelDetails = new TenantHostelDetailsRes(hostel.getHostelId(), hostel.getHostelName(),
                booking.getFloorId(), floor != null ? floor.getFloorName() : null, booking.getRoomId(),
                room != null ? room.getRoomName() : null, booking.getBedId(), bed != null ? bed.getBedName() : null);

        List<InvoiceResponse> invoiceResponses = invoices.stream()
                .map(invoice -> {
                    Users createdByUser = usersMap.getOrDefault(invoice.getCreatedBy(), null);
                    Users updatedByUser = usersMap.getOrDefault(invoice.getUpdatedBy(), null);
                    return new InvoiceResponseMapper(customer, createdByUser, updatedByUser)
                            .apply(invoice);
                }).toList();

        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(transaction -> {
                    InvoicesV1 invoice = invoiceMap.getOrDefault(transaction.getInvoiceId(), null);
                    BankingV1 bank = bankMap.getOrDefault(transaction.getBankId(), null);
                    Users createdByUser = usersMap.getOrDefault(transaction.getCreatedBy(), null);
                    Users updatedByUser = usersMap.getOrDefault(transaction.getUpdatedBy(), null);
                    return new TransactionResMapper(invoice, hostel, customer, bank,
                            createdByUser, updatedByUser)
                            .apply(transaction);
                }).toList();

        List<InvoiceRedemptionRes> invoiceRedemptionRes = invoiceRedemptions.stream()
                .map(redemption -> {
                    InvoicesV1 sourceInvoice = invoiceMap.getOrDefault(redemption.getSourceInvoiceId(), null);
                    InvoicesV1 targetInvoice = invoiceMap.getOrDefault(redemption.getTargetInvoiceId(), null);
                    Users redemptionCreatedByUser = usersMap.getOrDefault(redemption.getCreatedBy(), null);
                    String redemptionUpdatedBy = null;
                    if (UserType.OWNER.name().equals(redemption.getUserType())) {
                        Users updatedByUser = usersMap.getOrDefault(redemption.getUpdatedBy(), null);
                        if (updatedByUser != null){
                            redemptionUpdatedBy = Utils.getFullName(updatedByUser.getFirstName(), updatedByUser.getLastName());
                        }
                    } else if (UserType.AGENT.name().equals(redemption.getUserType())) {
                        Agent updatedByAgent = agentMap.getOrDefault(redemption.getUpdatedBy(), null);
                        if (updatedByAgent != null){
                            redemptionUpdatedBy = Utils.getFullName(updatedByAgent.getFirstName(), updatedByAgent.getLastName());
                        }
                    }
                    return new InvoiceRedemptionResMapper(hostel, targetInvoice, sourceInvoice,
                            redemptionCreatedByUser, redemptionUpdatedBy, customer)
                            .apply(redemption);
                }).toList();

        CustomerDetailsRes response = new CustomerDetailsRes(customer.getCustomerId(), customer.getFirstName(),
                customer.getLastName(), Utils.getFullName(customer.getFirstName(), customer.getLastName()),
                Utils.getInitials(customer.getFirstName(), customer.getLastName()), customer.getMobSerialNo(),
                Utils.maskMobileNo(customer.getMobile()), customer.getEmailId(), customer.getHouseNo(), customer.getStreet(),
                customer.getLandmark(), customer.getPincode(), customer.getCity(), customer.getState(), customer.getCountry(),
                Utils.buildFullAddress(customer), customer.getProfilePic(), customer.getCurrentStatus(),
                customer.getCustomerBedStatus(), customer.getKycStatus(), Utils.dateToString(customer.getJoiningDate()),
                Utils.dateToString(customer.getExpJoiningDate()), Utils.dateToString(customer.getDateOfBirth()),
                customer.getGender(), createdBy, updatedBy, createdAtDate, createdAtTime, updatedAtDate, updatedAtTime,
                hostelDetails, invoiceResponses, transactionResponses, invoiceRedemptionRes);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getCustomerSettlementInfo(String customerId, CustomerDatePayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Tenants.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Customers customer = customersRepository.findByCustomerId(customerId);
        if (customer == null){
            return new ResponseEntity<>(Utils.NO_CUSTOMER_FOUND, HttpStatus.BAD_REQUEST);
        }

        HostelV1 hostel = hostelService.getHostelByHostelId(customer.getHostelId());
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        BookingsV1 booking = bookingsService.getBookingInfoByCustomerId(customerId);
        if (booking == null){
            return new ResponseEntity<>(Utils.BOOKING_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (CustomerStatus.NOTICE.name().equals(customer.getCurrentStatus())){
            return new ResponseEntity<>("Customer is not in notice", HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        Date leavingDate = today;
        if (payload.date() != null){
            leavingDate = Utils.localDateToDate(payload.date());
        }

        if (Utils.compareWithTwoDates(leavingDate, today) > 0) {
            return new ResponseEntity<>("Future leaving date is not allowed", HttpStatus.BAD_REQUEST);
        }

        if (booking.getNoticeDate() != null) {
            if (Utils.compareWithTwoDates(booking.getNoticeDate(), leavingDate) > 0) {
                return new ResponseEntity<>("Leaving date must be after notice date", HttpStatus.BAD_REQUEST);
            }
        }

        BillingRules billingRule = billingRulesService.getCurrentMonthTemplate(customer.getHostelId());

        BillingDates billingDates = null;
        if (BillingType.FIXED_DATE.name().equals(billingRule.getTypeOfBilling())){
            billingDates = billingRulesService.computeBillingDatesWithBillingModel(billingRule, today);
        } else if (BillingType.JOINING_DATE_BASED.name().equals(billingRule.getTypeOfBilling())) {
            if (customer.getJoiningDate() == null){
                return new ResponseEntity<>("Joining date not found", HttpStatus.BAD_REQUEST);
            }
            billingDates = billingRulesService
                    .computeJoiningBillingDatesWithBillingModel(billingRule, customer.getJoiningDate(), today);
        }

        if (billingDates == null){
            return new ResponseEntity<>("Billing dates not found", HttpStatus.BAD_REQUEST);
        }

        if (Utils.compareWithTwoDates(leavingDate, billingDates.currentBillStartDate()) < 0) {
            return new ResponseEntity<>("Settlement can not be generated for older billing cycles", HttpStatus.BAD_REQUEST);
        }

        CustomersBedHistory latestBedHistory = customerBedHistoryService
                .getLatestBedHistoryByCustomerId(customerId);
        if (latestBedHistory == null){
            return new ResponseEntity<>("Customer bed history not found", HttpStatus.BAD_REQUEST);
        } else {
            if (Utils.compareWithTwoDates(latestBedHistory.getStartDate(), leavingDate) > 0) {
                return new ResponseEntity<>("Leaving date must be after joining date", HttpStatus.BAD_REQUEST);
            }
        }

        CustomerSettlementInfoRes response = null;

        if (BillingType.FIXED_DATE.name().equals(billingRule.getTypeOfBilling())){
            if (BillingModel.PREPAID.name().equals(billingRule.getBillingModel())){
                if (Utils.compareWithTwoDates(latestBedHistory.getStartDate(), billingDates.currentBillStartDate()) > 0) {
                    response = buildFixedDateBasedPrepaidBedChangeSettlementInfo();
                } else {
                    response = buildFixedDateBasedPrepaidSettlementInfo();
                }
            } else if (BillingModel.POSTPAID.name().equals(billingRule.getBillingModel())) {
                response = buildFixedDateBasedPostpaidSettlementInfo();
            }
        } else if (BillingType.JOINING_DATE_BASED.name().equals(billingRule.getTypeOfBilling())) {
            if (BillingModel.PREPAID.name().equals(billingRule.getBillingModel())){
                response = buildJoiningBasedPrepaidSettlementInfo(customer, booking,
                        leavingDate, hostel);
            } else if (BillingModel.POSTPAID.name().equals(billingRule.getBillingModel())) {
                //Joining based does not have postpaid yet
            }
        }

        if (response != null){
            SettlementDetails settlementDetails = settlementDetailsService
                    .addSettlementForCustomer(customerId, leavingDate);

            SettlementDetailsSnapshot snapshot = SnapshotUtility.toSnapshot(settlementDetails);

            agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.SETTLEMENT_DETAILS,
                    String.valueOf(settlementDetails.getId()), null, snapshot);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private CustomerSettlementInfoRes buildJoiningBasedPrepaidSettlementInfo(Customers customer,
                                                                             BookingsV1 booking,
                                                                             Date leavingDate,
                                                                             HostelV1 hostel) {

        String customerId = customer.getCustomerId();
        String hostelId = customer.getHostelId();
        boolean isBookingOrAdvancePaid = false;
        double totalBookingAndAdvancePaidAmount = 0.0;
        double bookingInvoicePaidAmount = 0.0;
        double totalDeductionAmount = 0.0;
        double paidDeductionAmount = 0.0;
        double pendingDeductionAmount = 0.0;
        double availableBookingAmountToRedeem = 0.0;
        double availableAdvanceAmountToRedeem = 0.0;
        double availableAmountToRedeem = 0.0;
        double advanceAmountRedeemedFromBookingInvoice = 0.0;

        InvoicesV1 bookingInvoice = invoiceV1Service
                .getInvoicesByCustomerIdAndInvoiceType(customerId, InvoiceType.BOOKING.name())
                .getFirst();

        InvoicesV1 advanceInvoice = invoiceV1Service
                .getInvoicesByCustomerIdAndInvoiceType(customerId, InvoiceType.ADVANCE.name())
                .getFirst();

        CustomerDeductionsInfoRes deductionsInfoRes = null;

        if (bookingInvoice != null) {
            if (bookingInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) ||
                    bookingInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                if (bookingInvoice.getPaidAmount() != null) {
                    isBookingOrAdvancePaid = true;
                    bookingInvoicePaidAmount = bookingInvoice.getPaidAmount();
                    totalBookingAndAdvancePaidAmount = totalBookingAndAdvancePaidAmount + bookingInvoice.getPaidAmount();
                }
                if (bookingInvoice.getBalanceAmount() != null) {
                    availableBookingAmountToRedeem = bookingInvoice.getBalanceAmount();
                    availableAmountToRedeem = availableAmountToRedeem + bookingInvoice.getBalanceAmount();
                }
            }
        }

        if (advanceInvoice != null) {
            advanceAmountRedeemedFromBookingInvoice = invoiceRedemptionService
                    .getInvoiceRedemptionByTargetInvoiceId(advanceInvoice.getInvoiceId())
                    .stream()
                    .mapToDouble(redemption ->
                            redemption.getRedemptionAmount() != null
                                    ? redemption.getRedemptionAmount() : 0)
                    .sum();

            if (advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) ||
                    advanceInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                if (advanceInvoice.getPaidAmount() != null) {
                    isBookingOrAdvancePaid = true;
                    totalBookingAndAdvancePaidAmount = totalBookingAndAdvancePaidAmount + advanceInvoice.getPaidAmount();
                    totalBookingAndAdvancePaidAmount = totalBookingAndAdvancePaidAmount - advanceAmountRedeemedFromBookingInvoice;
                }
                if (advanceInvoice.getBalanceAmount() != null) {
                    availableAdvanceAmountToRedeem = advanceInvoice.getBalanceAmount();
                    availableAmountToRedeem = availableAmountToRedeem + advanceInvoice.getBalanceAmount();
                }
            }

            if (advanceInvoice.getDeductions() != null &&
                    advanceInvoice.getDeductionAmount() != null &&
                    advanceInvoice.getDeductionAmount() > 0) {

                List<Deductions> deductions = advanceInvoice.getDeductions();

                if (!deductions.isEmpty()) {

                    List<DeductionsInfoRes> deductionsInfo = deductions.stream()
                            .filter(i -> i.getPaidAmount() == null ||
                                    i.getPaidAmount() < i.getAmount())
                            .map(i -> {
                                double pendingAmount = 0.0;
                                if (i.getPaidAmount() != null) {
                                    pendingAmount = i.getAmount() - i.getPaidAmount();
                                }
                                return new DeductionsInfoRes(i.getType(), i.getAmount(),
                                        i.getPaidAmount(), pendingAmount);
                            }).toList();

                    totalDeductionAmount = deductions.stream().mapToDouble(Deductions::getAmount).sum();
                    paidDeductionAmount = deductions.stream().mapToDouble(Deductions::getPaidAmount).sum();
                    pendingDeductionAmount = totalDeductionAmount - paidDeductionAmount;

                    deductionsInfoRes = new CustomerDeductionsInfoRes(totalDeductionAmount, paidDeductionAmount,
                            pendingDeductionAmount, deductionsInfo);
                }
            }
        }

        double customerAdvanceAmount = 0;
        Advance advance = customer.getAdvance();
        if (advance != null) {
            customerAdvanceAmount = advance.getAdvanceAmount();
        }

        String joiningDate = null;
        double bookingRentAmount = 0.0;
        if (booking != null){
            if (booking.getJoiningDate() != null){
                joiningDate = Utils.dateToString(booking.getJoiningDate());
            }
            if (booking.getRentAmount() != null){
                bookingRentAmount = booking.getRentAmount();
            }
        }

        AvailableRedemptionAmountRes availableRedemptionAmountRes = new AvailableRedemptionAmountRes(
                availableBookingAmountToRedeem, availableAdvanceAmountToRedeem, availableAmountToRedeem);

        CustomerInfoRes customerInfoRes = new CustomerInfoRes(customerId, customer.getFirstName(), customer.getLastName(),
                Utils.getFullName(customer.getFirstName(), customer.getLastName()), customer.getProfilePic(),
                Utils.getInitials(customer.getFirstName(), customer.getLastName()), "91", customer.getMobile(),
                joiningDate, customerAdvanceAmount, bookingRentAmount, isBookingOrAdvancePaid, totalBookingAndAdvancePaidAmount,
                bookingInvoicePaidAmount, availableRedemptionAmountRes);

        String bookedDate = null;
        String noticeDate = null;
        String requestedLeavingDate = null;
        String actualLeavingDate = Utils.dateToString(leavingDate);

        if (booking != null){
            bookedDate = Utils.dateToString(booking.getBookingDate());
            noticeDate = Utils.dateToString(booking.getNoticeDate());
            requestedLeavingDate = Utils.dateToString(booking.getLeavingDate());
        }

        CustomerStayInfoRes customerStayInfoRes = new CustomerStayInfoRes(bookedDate, noticeDate,
                requestedLeavingDate, actualLeavingDate);

        ElectricityConfig ebConfig;
        if (hostel != null){
            ebConfig = hostel.getElectricityConfig();
        } else {
            ebConfig = null;
        }

        CustomerEbInfoRes customerEbInfoRes = null;
        if (ebConfig != null){

            List<MissedEbRoomsRes> allMissedEbRoomsRes = new ArrayList<>();
            List<PendingEbRes> allPendingEbRes = new ArrayList<>();

            if (EBReadingType.ROOM_READING.name().equals(ebConfig.getTypeOfReading())){

                List<ElectricityReadings> allPendingEbReadings = new ArrayList<>();

                List<CustomersBedHistory> customersBedHistories = customerBedHistoryService
                        .findByHostelIdAndCustomerId(hostelId, customerId);

                Map<Integer, List<CustomersBedHistory>> roomHistoryMap = customersBedHistories.stream()
                        .collect(Collectors.groupingBy(CustomersBedHistory::getRoomId));

                Set<Integer> floorIds = customersBedHistories
                        .stream()
                        .map(CustomersBedHistory::getFloorId)
                        .collect(Collectors.toSet());

                List<Floors> floors = floorsService.getByFloorIds(floorIds);

                Map<Integer, Floors> floorsMap = floors.stream()
                        .collect(Collectors.toMap(Floors::getFloorId,
                                floor -> floor));

                Set<Integer> roomIds = customersBedHistories
                        .stream()
                        .map(CustomersBedHistory::getRoomId)
                        .collect(Collectors.toSet());

                List<Rooms> rooms = roomsService
                        .getRoomsByRoomIds(roomIds);

                Map<Integer, Rooms> roomsMap = rooms.stream()
                        .collect(Collectors.toMap(Rooms::getRoomId,
                                Function.identity()));

                Set<Integer> bedIds = customersBedHistories
                        .stream()
                        .map(CustomersBedHistory::getBedId)
                        .collect(Collectors.toSet());

                List<Beds> beds = bedsService
                        .getBedsByBedIds(bedIds);

                Map<Integer, Beds> bedsMap = beds.stream()
                        .collect(Collectors.toMap(Beds::getBedId,
                                Function.identity()));

                List<ElectricityReadings> latestReadingOfRooms = electricityReadingsService
                        .getLatestEntriesByHostelIdAndRoomIds(hostelId, roomIds);

                roomHistoryMap.forEach((roomId, histories) -> {

                    Date start = histories.stream()
                            .map(CustomersBedHistory::getStartDate)
                            .min(Date::compareTo)
                            .orElse(null);

                    Date end = histories.stream()
                            .map(h -> h.getEndDate() == null ? leavingDate : h.getEndDate())
                            .max(Date::compareTo)
                            .orElse(leavingDate);

                    List<ElectricityReadings> readings =
                            electricityReadingsService.getPendingReadingsBetweenDates(
                                    hostelId,
                                    roomId,
                                    start,
                                    end);

                    if (readings != null) {
                        allPendingEbReadings.addAll(readings);
                    }
                });

                if (!allPendingEbReadings.isEmpty()){

                    Map<Integer, ElectricityReadings> readingMap =
                            allPendingEbReadings.stream()
                                    .collect(Collectors.toMap(
                                            ElectricityReadings::getId,
                                            Function.identity(),
                                            (a, b) -> a));

                    List<ElectricityReadings> uniquePendingReadings =
                            new ArrayList<>(readingMap.values());

                    Set<Integer> allPendingEbReadingsIds = uniquePendingReadings.stream()
                            .map(ElectricityReadings::getId)
                            .collect(Collectors.toSet());

                    List<CustomersEbHistory> pendingCustomersEbHistories = customerEbHistoryService
                            .getAllByCustomerIdAndReadingId(customerId, new ArrayList<>(allPendingEbReadingsIds));

                    if (!pendingCustomersEbHistories.isEmpty()){
                        // Already calculated entries from CustomerEbHistory
                        List<PendingEbRes> ebHistoryResponses = buildPendingEbResponseByEbHistory(pendingCustomersEbHistories,
                                customersBedHistories, floorsMap, roomsMap, leavingDate);

                        allPendingEbRes.addAll(ebHistoryResponses);

                        // Find pending readings which are NOT present in CustomerEbHistory
                        Set<Integer> processedReadingIds = pendingCustomersEbHistories.stream()
                                .map(CustomersEbHistory::getReadingId)
                                .collect(Collectors.toSet());

                        List<ElectricityReadings> readingsToCalculate = uniquePendingReadings.stream()
                                .filter(reading -> !processedReadingIds.contains(reading.getId()))
                                .toList();

                        if (!readingsToCalculate.isEmpty()) {
                           allPendingEbRes.addAll(
                                   buildPendingEbResponse(readingsToCalculate, allPendingEbRes, roomHistoryMap,
                                   floorsMap, roomsMap, customerId, leavingDate, ebConfig)
                           );
                        }
                    } else {
                        allPendingEbRes.addAll(
                                buildPendingEbResponse(uniquePendingReadings, allPendingEbRes, roomHistoryMap,
                                floorsMap, roomsMap, customerId, leavingDate, ebConfig)
                        );
                    }
                }

                allMissedEbRoomsRes.addAll(
                        buildMissedEbRoomResponse(customersBedHistories, allMissedEbRoomsRes,
                        latestReadingOfRooms, floorsMap, roomsMap, bedsMap, leavingDate)
                );

            } else if (EBReadingType.FLAT_RATE.name().equals(ebConfig.getTypeOfReading())) {
                return null;
            } else if (EBReadingType.HOSTEL_READING.name().equals(ebConfig.getTypeOfReading())) {
                return null;
            } else {
                return null;
            }

            double pendingEbAmount = 0.0;
            if (!allPendingEbRes.isEmpty()) {
                pendingEbAmount = allPendingEbRes.stream()
                        .mapToDouble(PendingEbRes::amount)
                        .sum();
            }

            customerEbInfoRes = new CustomerEbInfoRes(0.0, ebConfig.getCharge(), "NA",
                    ebConfig.getTypeOfReading(), pendingEbAmount, false, true,
                    allMissedEbRoomsRes, allPendingEbRes);
        }

        List<UnpaidInvoicesInfoRes> unpaidInvoicesInfoRes = new ArrayList<>();

        CustomerRentInfoRes customerRentInfoRes = new CustomerRentInfoRes();

        CustomerWalletInfoRes customerWalletInfoRes = new CustomerWalletInfoRes();

        CustomerBookingInfoRes customerBookingInfoRes = new CustomerBookingInfoRes();

        CustomerAdvanceInfoRes customerAdvanceInfoRes = new CustomerAdvanceInfoRes();

        double totalRefundableAdvance = 0.0;
        if (isBookingOrAdvancePaid) {
            totalRefundableAdvance = availableAmountToRedeem;
        }

        CustomerFinalSettlementInfoRes finalSettlementInfoRes = new CustomerFinalSettlementInfoRes(0.0,
                pendingDeductionAmount, 0.0, 0.0, totalRefundableAdvance, 0.0,
                0.0, false, "", 0.0);

        return new CustomerSettlementInfoRes(customerInfoRes, customerStayInfoRes, customerEbInfoRes,
                unpaidInvoicesInfoRes, customerRentInfoRes, customerWalletInfoRes, customerBookingInfoRes,
                customerAdvanceInfoRes, deductionsInfoRes, finalSettlementInfoRes);
    }

    private List<PendingEbRes> buildPendingEbResponseByEbHistory(List<CustomersEbHistory> pendingCustomersEbHistories,
                                                                 List<CustomersBedHistory> customersBedHistories,
                                                                 Map<Integer, Floors> floorsMap, Map<Integer, Rooms> roomsMap,
                                                                 Date leavingDate) {

        if (pendingCustomersEbHistories == null || pendingCustomersEbHistories.isEmpty()) {
            return Collections.emptyList();
        }

        return pendingCustomersEbHistories.stream()
                .map(ebHistory -> {

                    String floorName = null;
                    String roomName = null;

                    Floors floor = floorsMap.getOrDefault(ebHistory.getFloorId(), null);
                    Rooms room = roomsMap.getOrDefault(ebHistory.getRoomId(), null);

                    floorName = floor != null ? floor.getFloorName() : null;
                    roomName = room != null ? room.getRoomName() : null;

                    Date startDate = ebHistory.getStartDate();
                    Date endDate = ebHistory.getEndDate();

                    if (customersBedHistories != null && !customersBedHistories.isEmpty()) {

                        Set<Integer> customerRoomIds = customersBedHistories.stream()
                                .map(CustomersBedHistory::getRoomId)
                                .collect(Collectors.toSet());

                        if (customerRoomIds.contains(ebHistory.getRoomId())
                                && leavingDate != null
                                && ebHistory.getEndDate() != null
                                && Utils.compareWithTwoDates(leavingDate, ebHistory.getEndDate()) < 0) {
                            endDate = leavingDate;
                        }
                    }

                    return new PendingEbRes(ebHistory.getFloorId(),
                            floorName, ebHistory.getRoomId(), roomName, Utils.roundOfDoubleTo2Digits(ebHistory.getUnits()),
                            Utils.roundOfDoubleTo2Digits(ebHistory.getAmount()), Utils.dateToString(startDate),
                            Utils.dateToString(endDate));
                }).toList();
    }

    private List<PendingEbRes> buildPendingEbResponse(List<ElectricityReadings> allPendingEbReadings,
                                                      List<PendingEbRes> allPendingEbRes,
                                                      Map<Integer, List<CustomersBedHistory>> roomHistoryMap,
                                                      Map<Integer, Floors> floorsMap, Map<Integer, Rooms> roomsMap,
                                                      String customerId, Date leavingDate, ElectricityConfig ebConfig) {

        double unitPrice;
        if (ebConfig != null){
            unitPrice = ebConfig.getCharge() != null ? ebConfig.getCharge() : 0;
        } else {
            unitPrice = 0;
        }

        allPendingEbReadings.forEach(item -> {

            String floorName = null;
            String roomName = null;

            Floors floor = floorsMap.getOrDefault(item.getFloorId(), null);
            Rooms room = roomsMap.getOrDefault(item.getRoomId(), null);

            floorName = floor != null ? floor.getFloorName() : null;
            roomName = room != null ? room.getRoomName() : null;

            Date finalBillStartDate = item.getBillStartDate();
            Date finalBillEndDate = item.getBillEndDate();
            long totalPersonDays = 0;

            List<CustomersBedHistory> customersInRoomBedHistoryBetweenDates = roomHistoryMap
                            .getOrDefault(item.getRoomId(), Collections.emptyList())
                            .stream()
                            .filter(history ->
                                    Utils.compareWithTwoDates(history.getStartDate(), item.getBillEndDate()) <= 0
                                            && (history.getEndDate() == null
                                            || Utils.compareWithTwoDates(history.getEndDate(), item.getBillStartDate()) >= 0))
                            .toList();

            if (!customersInRoomBedHistoryBetweenDates.isEmpty()) {
                for (CustomersBedHistory history : customersInRoomBedHistoryBetweenDates) {

                    // Customer occupied period for this room
                    Date historyStart = history.getStartDate();
                    Date historyEnd = history.getEndDate() == null ? leavingDate : history.getEndDate();

                    // Overlap with bill period
                    Date overlapStart = Utils.compareWithTwoDates(historyStart, item.getBillStartDate()) > 0
                            ? historyStart
                            : item.getBillStartDate();

                    Date overlapEnd = Utils.compareWithTwoDates(historyEnd, item.getBillEndDate()) < 0
                            ? historyEnd
                            : item.getBillEndDate();

                    if (Utils.compareWithTwoDates(overlapStart, overlapEnd) <= 0) {

                        totalPersonDays += Utils.findNumberOfDays(overlapStart, overlapEnd);

                        if (history.getCustomerId().equals(customerId)) {
                            // Update final output period
                            if (Utils.compareWithTwoDates(overlapStart, finalBillStartDate) > 0) {
                                finalBillStartDate = overlapStart;
                            }

                            if (Utils.compareWithTwoDates(overlapEnd, finalBillEndDate) < 0) {
                                finalBillEndDate = overlapEnd;
                            }
                        }
                    }
                }
            }

            if (totalPersonDays <= 0){
                totalPersonDays = 1;
            }
            double unitsPerPersonPerDay = item.getConsumption() / totalPersonDays;
            long noOfDaysStayed = Utils.findNumberOfDays(finalBillStartDate, finalBillEndDate);
            double totalUnitsPerPerson = unitsPerPersonPerDay * noOfDaysStayed;
            double price = totalUnitsPerPerson * unitPrice;

            PendingEbRes pendingEbForSettlementRes = new PendingEbRes(item.getFloorId(),
                    floorName, item.getRoomId(), roomName, Utils.roundOfDoubleTo2Digits(totalUnitsPerPerson),
                    Utils.roundOfDoubleTo2Digits(price), Utils.dateToString(finalBillStartDate),
                    Utils.dateToString(finalBillEndDate));

            allPendingEbRes.add(pendingEbForSettlementRes);
        });

        return allPendingEbRes;
    }

    private List<MissedEbRoomsRes> buildMissedEbRoomResponse(List<CustomersBedHistory> customersBedHistories,
                                                             List<MissedEbRoomsRes> allMissedEbRoomsRes,
                                                             List<ElectricityReadings> latestReadingOfRooms,
                                                             Map<Integer, Floors> floorsMap, Map<Integer, Rooms> roomsMap,
                                                             Map<Integer, Beds> bedsMap, Date leavingDate) {

        Map<Integer, ElectricityReadings> latestReadingMap = latestReadingOfRooms.stream()
                        .collect(Collectors.toMap(
                                ElectricityReadings::getRoomId,
                                Function.identity(),
                                (a, b) -> a
                        ));

        customersBedHistories.forEach(item -> {

            ElectricityReadings latestReadingOfIndividualRoom = latestReadingMap.get(item.getRoomId());

            Date endDate = item.getEndDate();
            if (item.getEndDate() == null) {
                endDate = leavingDate;
            }

            String floorName = null;
            String roomName = null;
            String bedName = null;
            String fromDate = null;
            String toDate = null;
            String lastEntryDate = null;
            Double lastReading = 0.0;

            Floors floor = floorsMap.getOrDefault(item.getFloorId(), null);
            Rooms room = roomsMap.getOrDefault(item.getRoomId(), null);
            Beds bed = bedsMap.getOrDefault(item.getBedId(), null);

            floorName = floor != null ? floor.getFloorName() : null;
            roomName = room != null ? room.getRoomName() : null;
            bedName = bed != null ? bed.getBedName() : null;

            fromDate = Utils.dateToString(item.getStartDate());
            if (item.getEndDate() == null) {
                toDate = Utils.dateToString(leavingDate);
            } else {
                toDate = Utils.dateToString(item.getEndDate());
            }

            if (latestReadingOfIndividualRoom != null &&
                    Utils.compareWithTwoDates(latestReadingOfIndividualRoom.getEntryDate(), endDate) < 0) {

                lastEntryDate = Utils.dateToString(latestReadingOfIndividualRoom.getEntryDate());
                lastReading = latestReadingOfIndividualRoom.getCurrentReading();
                if (Utils.compareWithTwoDates(latestReadingOfIndividualRoom.getEntryDate(), item.getStartDate()) > 0) {
                    fromDate = Utils.dateToString(Utils.addDaysToDate(latestReadingOfIndividualRoom.getEntryDate(), 1));
                }
            }

            MissedEbRoomsRes missedEbRoomsRes = new MissedEbRoomsRes(item.getFloorId(), floorName,
                    item.getRoomId(), roomName, item.getBedId(), bedName, fromDate, toDate,
                    lastReading, lastEntryDate);

            allMissedEbRoomsRes.add(missedEbRoomsRes);
        });

        return allMissedEbRoomsRes;
    }
}
