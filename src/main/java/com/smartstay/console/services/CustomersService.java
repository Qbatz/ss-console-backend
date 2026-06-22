package com.smartstay.console.services;

import com.smartstay.console.Mapper.customers.CustomerSumMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.customers.CustomerResetSnapshot;
import com.smartstay.console.dto.customers.CustomersCredentialsSnapshot;
import com.smartstay.console.dto.customers.CustomersSnapshot;
import com.smartstay.console.dto.customers.Deductions;
import com.smartstay.console.ennum.*;
import com.smartstay.console.payloads.customers.CustomerResetPayload;
import com.smartstay.console.repositories.CustomersRepository;
import com.smartstay.console.responses.customers.*;
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

        Set<String> userIds = new HashSet<>();
        userIds.add(customer.getCreatedBy());
        userIds.add(customer.getUpdatedBy());

        List<Users> users = usersService.getUsersByIds(userIds);
        Map<String, Users> usersMap = users.stream()
                .collect(Collectors.toMap(Users::getUserId, user -> user));

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

        CustomerDetailsRes response = new CustomerDetailsRes(customer.getCustomerId(), customer.getHostelId(),
                hostel.getHostelName(), customer.getFirstName(), customer.getLastName(), Utils.getFullName(customer.getFirstName(),
                customer.getLastName()), Utils.getInitials(customer.getFirstName(), customer.getLastName()),
                customer.getMobSerialNo(), customer.getMobile(), customer.getEmailId(), customer.getHouseNo(),
                customer.getStreet(), customer.getLandmark(), customer.getPincode(), customer.getCity(),
                customer.getState(), customer.getCountry(), Utils.buildFullAddress(customer), customer.getProfilePic(),
                customer.getCurrentStatus(), customer.getCustomerBedStatus(), customer.getKycStatus(),
                Utils.dateToString(customer.getJoiningDate()), Utils.dateToString(customer.getExpJoiningDate()),
                Utils.dateToString(customer.getDateOfBirth()), customer.getGender(), createdBy, updatedBy,
                createdAtDate, createdAtTime, updatedAtDate, updatedAtTime);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
