package com.smartstay.console.services;

import com.smartstay.console.Mapper.customers.CustomerSumMapper;
import com.smartstay.console.Mapper.invoice.InvoiceResponseMapper;
import com.smartstay.console.Mapper.invoiceRedemption.InvoiceRedemptionResMapper;
import com.smartstay.console.Mapper.transaction.TransactionResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dao.InvoiceItems;
import com.smartstay.console.dto.customers.CustomerResetSnapshot;
import com.smartstay.console.dto.customers.CustomersCredentialsSnapshot;
import com.smartstay.console.dto.customers.CustomersSnapshot;
import com.smartstay.console.dto.customers.Deductions;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.dto.settlementDetails.SettlementDetailsSnapshot;
import com.smartstay.console.ennum.*;
import com.smartstay.console.exceptions.BadRequestException;
import com.smartstay.console.payloads.customers.CusSettlementDeductionsPayload;
import com.smartstay.console.payloads.customers.CustomerDatePayload;
import com.smartstay.console.payloads.customers.CustomerResetPayload;
import com.smartstay.console.payloads.customers.CustomerSettlementGeneratePayload;
import com.smartstay.console.repositories.CustomersRepository;
import com.smartstay.console.responses.customers.*;
import com.smartstay.console.responses.invoice.InvoiceResponse;
import com.smartstay.console.responses.invoiceRedemption.InvoiceRedemptionRes;
import com.smartstay.console.responses.transaction.TransactionResponse;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import jakarta.validation.Valid;
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
    @Autowired
    private AmenitiesService amenitiesService;

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
        SettlementDetails settlementDetails = settlementDetailsService.findByCustomerId(customerId);
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
        if (settlementDetails != null) {
            settlementDetailsService.delete(settlementDetails);
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

        KycDetails kycDetails = customer.getKycDetails();

        String kycDetailsStatus = null;
        boolean canApproveKyc = false;
        if (kycDetails != null) {
            kycDetailsStatus = kycDetails.getCurrentStatus();
            if (KycStatus.WAITING_FOR_APPROVAL.name().equalsIgnoreCase(kycDetailsStatus)) {
                canApproveKyc = true;
            }
        }

        boolean canGenerateSettlement = false;
        if (CustomerStatus.NOTICE.name().equals(customer.getCurrentStatus())){
            canGenerateSettlement = true;
        }

        CustomerDetailsRes response = new CustomerDetailsRes(customer.getCustomerId(), customer.getFirstName(),
                customer.getLastName(), Utils.getFullName(customer.getFirstName(), customer.getLastName()),
                Utils.getInitials(customer.getFirstName(), customer.getLastName()), customer.getMobSerialNo(),
                Utils.maskMobileNo(customer.getMobile()), customer.getEmailId(), customer.getHouseNo(), customer.getStreet(),
                customer.getLandmark(), customer.getPincode(), customer.getCity(), customer.getState(), customer.getCountry(),
                Utils.buildFullAddress(customer), customer.getProfilePic(), customer.getCurrentStatus(),
                customer.getCustomerBedStatus(), customer.getKycStatus(), kycDetailsStatus,
                Utils.dateToString(customer.getJoiningDate()), Utils.dateToString(customer.getExpJoiningDate()),
                Utils.dateToString(customer.getDateOfBirth()), customer.getGender(), createdBy, updatedBy,
                createdAtDate, createdAtTime, updatedAtDate, updatedAtTime, canApproveKyc, canGenerateSettlement,
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

        if (!CustomerStatus.NOTICE.name().equals(customer.getCurrentStatus())){
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
        if (billingRule == null){
            return new ResponseEntity<>(Utils.BILLING_RULE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        BillingDates billingDates = null;
        if (BillingType.FIXED_DATE.name().equals(billingRule.getTypeOfBilling())){
            billingDates = billingRulesService.computeBillingDates(billingRule, leavingDate);
        } else if (BillingType.JOINING_DATE_BASED.name().equals(billingRule.getTypeOfBilling())) {
            if (customer.getJoiningDate() == null){
                return new ResponseEntity<>("Joining date not found", HttpStatus.BAD_REQUEST);
            }
            billingDates = billingRulesService
                    .computeJoiningBasedBillingDates(billingRule, customer.getJoiningDate(), leavingDate);
        }

        if (billingDates == null){
            return new ResponseEntity<>("Billing dates not found", HttpStatus.BAD_REQUEST);
        }

//        if (Utils.compareWithTwoDates(leavingDate, billingDates.currentBillStartDate()) < 0) {
//            return new ResponseEntity<>("Settlement can not be generated for older billing cycles", HttpStatus.BAD_REQUEST);
//        }

        List<InvoicesV1> invoicesAfterLeavingDate = invoiceV1Service
                .getInvoicesByCustomerIdAndStartDateAfter(customerId, leavingDate);
        if (!invoicesAfterLeavingDate.isEmpty()){
            return new ResponseEntity<>(invoicesAfterLeavingDate.size() +
                    " invoices exist after checkout date, delete them to generate settlement", HttpStatus.BAD_REQUEST);
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

        List<InvoicesV1> bookingInvoices = invoiceV1Service
                .getInvoicesByCustomerIdAndInvoiceType(customerId, InvoiceType.BOOKING.name());

        InvoicesV1 bookingInvoice = bookingInvoices.isEmpty()
                ? null
                : bookingInvoices.getFirst();

        List<InvoicesV1> advanceInvoices = invoiceV1Service
                .getInvoicesByCustomerIdAndInvoiceType(customerId, InvoiceType.ADVANCE.name());

        InvoicesV1 advanceInvoice = advanceInvoices.isEmpty()
                ? null
                : advanceInvoices.getFirst();

        List<CustomersBedHistory> customersBedHistories = customerBedHistoryService
                .findByHostelIdAndCustomerId(customer.getHostelId(), customerId);

        Set<Integer> bedIds = customersBedHistories
                .stream()
                .map(CustomersBedHistory::getBedId)
                .collect(Collectors.toSet());

        List<Beds> beds = bedsService.getBedsByBedIds(bedIds);

        Map<Integer, Beds> bedsMap = beds.stream()
                .collect(Collectors.toMap(Beds::getBedId,
                        Function.identity()));

        Set<Integer> roomIds = beds
                .stream()
                .map(Beds::getRoomId)
                .collect(Collectors.toSet());

        List<Rooms> rooms = roomsService.getRoomsByRoomIds(roomIds);

        Map<Integer, Rooms> roomsMap = rooms.stream()
                .collect(Collectors.toMap(Rooms::getRoomId,
                        Function.identity()));

        Set<Integer> floorIds = rooms
                .stream()
                .map(Rooms::getFloorId)
                .collect(Collectors.toSet());

        List<Floors> floors = floorsService.getByFloorIds(floorIds);

        Map<Integer, Floors> floorsMap = floors.stream()
                .collect(Collectors.toMap(Floors::getFloorId,
                        floor -> floor));

        CustomerSettlementInfoRes response = null;

        if (BillingType.FIXED_DATE.name().equals(billingRule.getTypeOfBilling())){
            if (BillingModel.PREPAID.name().equals(billingRule.getBillingModel())){
                if (Utils.compareWithTwoDates(latestBedHistory.getStartDate(), billingDates.currentBillStartDate()) > 0) {
                    response = buildFixedDateBasedPrepaidBedChangeSettlementInfo(customer, booking,
                            leavingDate, hostel, billingDates, latestBedHistory, floorsMap, roomsMap, bedsMap,
                            bookingInvoice, advanceInvoice);
                } else {
                    response = buildFixedDateBasedPrepaidSettlementInfo(customer, booking,
                            leavingDate, hostel, billingDates, latestBedHistory, floorsMap, roomsMap, bedsMap,
                            bookingInvoice, advanceInvoice);
                }
            } else if (BillingModel.POSTPAID.name().equals(billingRule.getBillingModel())) {
                response = buildFixedDateBasedPostpaidSettlementInfo(customer, booking,
                        leavingDate, hostel, billingDates, latestBedHistory, floorsMap, roomsMap, bedsMap,
                        bookingInvoice, advanceInvoice);
            }
        } else if (BillingType.JOINING_DATE_BASED.name().equals(billingRule.getTypeOfBilling())) {
            if (BillingModel.PREPAID.name().equals(billingRule.getBillingModel())){
                response = buildJoiningBasedPrepaidSettlementInfo(customer, booking,
                        leavingDate, hostel, billingDates, latestBedHistory, floorsMap, roomsMap, bedsMap,
                        bookingInvoice, advanceInvoice);
            } else if (BillingModel.POSTPAID.name().equals(billingRule.getBillingModel())) {
                //Joining based does not have postpaid yet
            }
        }

        if (response != null){
            settlementDetailsService.addSettlementForCustomer(customerId, leavingDate, agent);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> generateCustomerSettlement(String customerId,
                                                        CustomerSettlementGeneratePayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Tenants.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Customers customer = customersRepository.findByCustomerId(customerId);
        if (customer == null){
            return new ResponseEntity<>(Utils.NO_CUSTOMER_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (CustomerStatus.SETTLEMENT_GENERATED.name().equals(customer.getCurrentStatus())) {
            return new ResponseEntity<>("Settlement already generated", HttpStatus.BAD_REQUEST);
        }

        if (!CustomerStatus.NOTICE.name().equals(customer.getCurrentStatus())){
            return new ResponseEntity<>("Customer is not in notice", HttpStatus.BAD_REQUEST);
        }

        BookingsV1 booking = bookingsService.getBookingInfoByCustomerId(customerId);
        if (booking == null){
            return new ResponseEntity<>(Utils.BOOKING_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        String hostelId = customer.getHostelId();
        HostelV1 hostel = hostelService.getHostelByHostelId(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        HostelPlan hostelPlan = hostel.getHostelPlan();
        if (hostelPlan != null && hostelPlan.getCurrentPlanEndsAt() != null){

            Date startOfToday = Utils.getStartOfDay(today);
            Date startOfEndsDate = Utils.getStartOfDay(hostelPlan.getCurrentPlanEndsAt());

            if (startOfToday.after(startOfEndsDate)){
                return new ResponseEntity<>(Utils.SUBSCRIPTION_NOT_ACTIVE, HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(Utils.INVALID_SUBSCRIPTION, HttpStatus.BAD_REQUEST);
        }

        SettlementDetails settlementDetails = settlementDetailsService
                .findByCustomerId(customerId);
        if (settlementDetails == null){
            return new ResponseEntity<>(Utils.SETTLEMENT_DETAILS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Date leavingDate = settlementDetails.getLeavingDate();
        if (leavingDate == null){
            return new ResponseEntity<>("Settlement details leaving date is null", HttpStatus.BAD_REQUEST);
        }

        if (Utils.compareWithTwoDates(leavingDate, today) > 0) {
            return new ResponseEntity<>("Future leaving date is not allowed", HttpStatus.BAD_REQUEST);
        }

        if (booking.getNoticeDate() != null) {
            if (Utils.compareWithTwoDates(booking.getNoticeDate(), leavingDate) > 0) {
                return new ResponseEntity<>("Leaving date must be after notice date", HttpStatus.BAD_REQUEST);
            }
        }

        BillingRules billingRule = billingRulesService.getCurrentMonthTemplate(hostelId);
        if (billingRule == null){
            return new ResponseEntity<>(Utils.BILLING_RULE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        BillingDates billingDates = null;
        if (BillingType.FIXED_DATE.name().equals(billingRule.getTypeOfBilling())){
            billingDates = billingRulesService.computeBillingDates(billingRule, leavingDate);
        } else if (BillingType.JOINING_DATE_BASED.name().equals(billingRule.getTypeOfBilling())) {
            if (customer.getJoiningDate() == null){
                return new ResponseEntity<>("Joining date not found", HttpStatus.BAD_REQUEST);
            }
            billingDates = billingRulesService
                    .computeJoiningBasedBillingDates(billingRule, customer.getJoiningDate(), leavingDate);
        }

        if (billingDates == null){
            return new ResponseEntity<>("Billing dates not found", HttpStatus.BAD_REQUEST);
        }

//        if (Utils.compareWithTwoDates(leavingDate, billingDates.currentBillStartDate()) < 0) {
//            return new ResponseEntity<>("Settlement can not be generated for older billing cycles", HttpStatus.BAD_REQUEST);
//        }

        List<InvoicesV1> invoicesAfterLeavingDate = invoiceV1Service
                .getInvoicesByCustomerIdAndStartDateAfter(customerId, leavingDate);
        if (!invoicesAfterLeavingDate.isEmpty()){
            return new ResponseEntity<>(invoicesAfterLeavingDate.size() +
                    " invoices exist after checkout date, delete them to generate settlement", HttpStatus.BAD_REQUEST);
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

        List<InvoicesV1> bookingInvoices = invoiceV1Service
                .getInvoicesByCustomerIdAndInvoiceType(customerId, InvoiceType.BOOKING.name());

        InvoicesV1 bookingInvoice = bookingInvoices.isEmpty()
                ? null
                : bookingInvoices.getFirst();

        List<InvoicesV1> advanceInvoices = invoiceV1Service
                .getInvoicesByCustomerIdAndInvoiceType(customerId, InvoiceType.ADVANCE.name());

        InvoicesV1 advanceInvoice = advanceInvoices.isEmpty()
                ? null
                : advanceInvoices.getFirst();

        List<InvoicesV1> settlementInvoices = invoiceV1Service
                .getInvoicesByCustomerIdAndInvoiceType(customerId, InvoiceType.SETTLEMENT.name());

        InvoicesV1 settlementInvoice = settlementInvoices.isEmpty()
                ? null
                : settlementInvoices.getFirst();

        List<CustomersBedHistory> customersBedHistories = customerBedHistoryService
                .findByHostelIdAndCustomerId(hostelId, customerId);

        Set<Integer> bedIds = customersBedHistories
                .stream()
                .map(CustomersBedHistory::getBedId)
                .collect(Collectors.toSet());

        List<Beds> beds = bedsService.getBedsByBedIds(bedIds);

        Map<Integer, Beds> bedsMap = beds.stream()
                .collect(Collectors.toMap(Beds::getBedId,
                        Function.identity()));

        Set<Integer> roomIds = beds
                .stream()
                .map(Beds::getRoomId)
                .collect(Collectors.toSet());

        List<Rooms> rooms = roomsService.getRoomsByRoomIds(roomIds);

        Map<Integer, Rooms> roomsMap = rooms.stream()
                .collect(Collectors.toMap(Rooms::getRoomId,
                        Function.identity()));

        Set<Integer> floorIds = rooms
                .stream()
                .map(Rooms::getFloorId)
                .collect(Collectors.toSet());

        List<Floors> floors = floorsService.getByFloorIds(floorIds);

        Map<Integer, Floors> floorsMap = floors.stream()
                .collect(Collectors.toMap(Floors::getFloorId,
                        floor -> floor));

        CustomerSettlementInfoRes response = null;

        if (BillingType.FIXED_DATE.name().equals(billingRule.getTypeOfBilling())){
            if (BillingModel.PREPAID.name().equals(billingRule.getBillingModel())){
                if (Utils.compareWithTwoDates(latestBedHistory.getStartDate(), billingDates.currentBillStartDate()) > 0) {
                    response = buildFixedDateBasedPrepaidBedChangeSettlementInfo(customer, booking,
                            leavingDate, hostel, billingDates, latestBedHistory, floorsMap, roomsMap, bedsMap,
                            bookingInvoice, advanceInvoice);
                } else {
                    response = buildFixedDateBasedPrepaidSettlementInfo(customer, booking,
                            leavingDate, hostel, billingDates, latestBedHistory, floorsMap, roomsMap, bedsMap,
                            bookingInvoice, advanceInvoice);
                }
            } else if (BillingModel.POSTPAID.name().equals(billingRule.getBillingModel())) {
                response = buildFixedDateBasedPostpaidSettlementInfo(customer, booking,
                        leavingDate, hostel, billingDates, latestBedHistory, floorsMap, roomsMap, bedsMap,
                        bookingInvoice, advanceInvoice);
            }
        } else if (BillingType.JOINING_DATE_BASED.name().equals(billingRule.getTypeOfBilling())) {
            if (BillingModel.PREPAID.name().equals(billingRule.getBillingModel())){
                response = buildJoiningBasedPrepaidSettlementInfo(customer, booking,
                        leavingDate, hostel, billingDates, latestBedHistory, floorsMap, roomsMap, bedsMap,
                        bookingInvoice, advanceInvoice);
            } else if (BillingModel.POSTPAID.name().equals(billingRule.getBillingModel())) {
                //Joining based does not have postpaid yet
            }
        }

        List<Deductions> advInvDeductions = advanceInvoice != null ?
                advanceInvoice.getDeductions() : new ArrayList<>();
        List<InvoicesV1> cancelledInvoices = new ArrayList<>();

        boolean isCustomRent = payload.isCustomRent();
        double customRentAmount = payload.customRentAmount();
        List<CusSettlementDeductionsPayload> newDeductions = payload.newDeductions();

        if (response != null){

            double totalAmountToBePaid = 0;
            double finalDeductionPendingAmount = 0;
            boolean isDiscounted = false;
            double currentMonthTotalAmount = 0;
            double currentMonthPaidRent = 0;
            double bookingAvailableBalance = 0;
            double advanceAvailableBalance = 0;

            CustomerFinalSettlementInfoRes finalSettlementInfoRes = response.customerFinalSettlementInfo();
            UnpaidInvoicesInfoRes unpaidInvoicesInfoRes = response.unpaidInvoicesInfo();
            CustomerRentInfoRes rentInfoRes = response.customerRentInfo();
            CustomerBookingInfoRes bookingInfoRes = response.customerBookingInfo();
            CustomerAdvanceInfoRes advanceInfoRes = response.customerAdvanceInfo();

            if (unpaidInvoicesInfoRes != null){
                List<UnpaidInvoicesRes> unpaidInvoicesRes = unpaidInvoicesInfoRes.unpaidInvoices() != null ?
                        unpaidInvoicesInfoRes.unpaidInvoices() : new ArrayList<>();

                Set<String> unpaidInvoicesIds = unpaidInvoicesRes.stream()
                        .map(UnpaidInvoicesRes::invoiceId)
                        .collect(Collectors.toSet());

                List<InvoicesV1> unpaidInvoices = invoiceV1Service
                        .getInvoicesByIds(unpaidInvoicesIds);

                for (InvoicesV1 unpaidInvoice : unpaidInvoices) {
                    unpaidInvoice.setCancelled(true);
                    unpaidInvoice.setCancelledDate(today);
                    unpaidInvoice.setUpdatedBy(authentication.getName());
                    unpaidInvoice.setUpdatedAt(today);

                    cancelledInvoices.add(unpaidInvoice);
                }
            }

            if (finalSettlementInfoRes != null){

                double currentMonthPayableRent = finalSettlementInfoRes.currentMonthPayableRent() != null ?
                        finalSettlementInfoRes.currentMonthPayableRent() : 0;
                currentMonthPaidRent = finalSettlementInfoRes.currentMonthPaidRent() != null ?
                        finalSettlementInfoRes.currentMonthPaidRent() : 0;
                double totalDeductionPendingAmount = finalSettlementInfoRes.totalDeductions() != null ?
                        finalSettlementInfoRes.totalDeductions() : 0;
                double ebAmount = finalSettlementInfoRes.ebAmount() != null ?
                        finalSettlementInfoRes.ebAmount() : 0;
                double walletAmount = finalSettlementInfoRes.walletAmount() != null ?
                        finalSettlementInfoRes.walletAmount() : 0;
                double discountAmount = finalSettlementInfoRes.discountAmount() != null ?
                        finalSettlementInfoRes.discountAmount() : 0;
                double refundableAdvanceAmount = finalSettlementInfoRes.refundableAdvance() != null ?
                        finalSettlementInfoRes.refundableAdvance() : 0;
                double unpaidInvoicesAmount = finalSettlementInfoRes.unpaidInvoiceAmount() != null ?
                        finalSettlementInfoRes.unpaidInvoiceAmount() : 0;
                double otherItemAmount = finalSettlementInfoRes.otherItemAmount() != null ?
                        finalSettlementInfoRes.otherItemAmount() : 0;

                if (discountAmount > 0) {
                    isDiscounted = true;
                }

                if (isCustomRent){
//                    if (customRentAmount < currentMonthPaidRent) {
//                        return new ResponseEntity<>("Custom rent can not be less than current month paid rent",
//                                HttpStatus.BAD_REQUEST);
//                    }
                    currentMonthPayableRent = customRentAmount;
                }

                if (newDeductions != null && !newDeductions.isEmpty()){
                    double newDeductionPendingAmount = 0;

                    for (CusSettlementDeductionsPayload deduction : newDeductions){
                        if (deduction.type() == null || deduction.type().isBlank()){
                            return new ResponseEntity<>("Deduction type is null or blank", HttpStatus.BAD_REQUEST);
                        }
                        if (deduction.amount() <= 0){
                            return new ResponseEntity<>("Deduction amount must be above 0", HttpStatus.BAD_REQUEST);
                        }
                        newDeductionPendingAmount += deduction.amount();

                        Deductions deductions = new Deductions();
                        deductions.setType(deduction.type());
                        deductions.setAmount(deduction.amount());
                        deductions.setPaidAmount(0.0);

                        advInvDeductions.add(deductions);
                    }

                    totalDeductionPendingAmount = totalDeductionPendingAmount + newDeductionPendingAmount;
                }

                finalDeductionPendingAmount = Utils.roundOfDoubleTo2Digits(totalDeductionPendingAmount);

                totalAmountToBePaid = unpaidInvoicesAmount + currentMonthPayableRent - currentMonthPaidRent
                        + otherItemAmount + finalDeductionPendingAmount + ebAmount + walletAmount
                        - discountAmount - refundableAdvanceAmount;

                totalAmountToBePaid = Utils.roundOfDoubleTo2Digits(totalAmountToBePaid);
            }

            if (rentInfoRes != null) {
                currentMonthTotalAmount = rentInfoRes.currentMonthTotalAmount() != null ?
                        rentInfoRes.currentMonthTotalAmount() : 0;
            }

            if (bookingInfoRes != null){
                bookingAvailableBalance = bookingInfoRes.availableBalance() != null ?
                        bookingInfoRes.availableBalance() : 0;
            }

            if (advanceInfoRes != null){
                advanceAvailableBalance = advanceInfoRes.availableBalance() != null ?
                        advanceInfoRes.availableBalance() : 0;
            }

            if (advanceInvoice != null){
                advanceInvoice.setDeductions(advInvDeductions);
            }

            List<String> cancelledInvoiceIds = cancelledInvoices.stream()
                    .map(InvoicesV1::getInvoiceId)
                    .toList();

            if (settlementInvoice == null) {
                settlementInvoice = new InvoicesV1();

                settlementInvoice.setCustomerId(customerId);
                settlementInvoice.setHostelId(hostelId);
                settlementInvoice.setInvoiceNumber(invoiceV1Service
                        .generateInvoiceNumber(hostelId, InvoiceType.RENT.name()));
                settlementInvoice.setCustomerMobile(customer.getMobile());
                settlementInvoice.setCustomerMailId(customer.getEmailId());
                settlementInvoice.setInvoiceType(InvoiceType.SETTLEMENT.name());
                settlementInvoice.setCreatedBy(authentication.getName());
                settlementInvoice.setCreatedAt(today);
            } else {
                settlementInvoice.setUpdatedBy(authentication.getName());
                settlementInvoice.setUpdatedAt(today);
            }

            settlementInvoice.setBasePrice(totalAmountToBePaid);
            settlementInvoice.setTotalAmount(totalAmountToBePaid);
            settlementInvoice.setPaidAmount(0.0);
            settlementInvoice.setBalanceAmount(0.0);
            settlementInvoice.setSubTotal(totalAmountToBePaid);
            settlementInvoice.setGst(0.0);
            settlementInvoice.setCgst(0.0);
            settlementInvoice.setSgst(0.0);
            settlementInvoice.setGstPercentile(0.0);
            if (Utils.roundOfDouble(totalAmountToBePaid) > 0) {
                settlementInvoice.setPaymentStatus(PaymentStatus.PENDING.name());
            } else if (Utils.roundOfDouble(totalAmountToBePaid) == 0) {
                settlementInvoice.setPaymentStatus(PaymentStatus.PAID.name());
            } else {
                settlementInvoice.setPaymentStatus(PaymentStatus.PENDING_REFUND.name());
            }
            settlementInvoice.setDeductionAmount(finalDeductionPendingAmount);
            settlementInvoice.setOthersDescription(null);
            settlementInvoice.setInvoiceMode(InvoiceMode.MANUAL.name());
            settlementInvoice.setCancelled(false);
            settlementInvoice.setDiscounted(isDiscounted);
            settlementInvoice.setCancelledInvoices(cancelledInvoiceIds);
            settlementInvoice.setDeductions(advInvDeductions);
            settlementInvoice.setInvoiceUrl(null);
            settlementInvoice.setInvoiceGeneratedDate(today);
            settlementInvoice.setCancelledDate(null);
            settlementInvoice.setInvoiceDueDate(leavingDate);
            settlementInvoice.setInvoiceDate(today);
            settlementInvoice.setInvoiceStartDate(leavingDate);
            settlementInvoice.setInvoiceEndDate(leavingDate);

            InvoicesV1 finalSettlementInvoice = settlementInvoice;
            List<InvoiceItems> invoiceItems = advInvDeductions.stream()
                    .map(i -> {
                        InvoiceItems item = new InvoiceItems();

                        item.setAmount(i.getAmount());
                        if (i.getType().equalsIgnoreCase(com.smartstay.console.ennum.InvoiceItems.MAINTENANCE.name())) {
                            item.setInvoiceItem(com.smartstay.console.ennum.InvoiceItems.MAINTENANCE.name());
                        } else if (i.getType().equalsIgnoreCase(com.smartstay.console.ennum.InvoiceItems.EB.name())) {
                            item.setInvoiceItem(com.smartstay.console.ennum.InvoiceItems.EB.name());
                        } else if (i.getType().equalsIgnoreCase(com.smartstay.console.ennum.InvoiceItems.AMENITY.name())) {
                            item.setInvoiceItem(com.smartstay.console.ennum.InvoiceItems.AMENITY.name());
                        } else if (i.getType().equalsIgnoreCase(com.smartstay.console.ennum.InvoiceItems.RENT.name())) {
                            item.setInvoiceItem(com.smartstay.console.ennum.InvoiceItems.RENT.name());
                        } else {
                            item.setInvoiceItem(com.smartstay.console.ennum.InvoiceItems.OTHERS.name());
                            item.setOtherItem(i.getType());
                        }
                        item.setInvoice(finalSettlementInvoice);

                        return item;
                    }).toList();
            settlementInvoice.setInvoiceItems(invoiceItems);

            settlementInvoice = invoiceV1Service.save(settlementInvoice);

            SettlementItems settlementItems = settlementItemsService.getByInvoiceId(settlementInvoice.getInvoiceId());
            if (settlementItems == null){
                settlementItems = new SettlementItems();
                settlementItems.setInvoiceId(settlementInvoice.getInvoiceId());
                settlementItems.setCreateAt(today);
                settlementItems.setCreatedBy(authentication.getName());
            }

            settlementItems.setHostelId(hostelId);
            settlementItems.setCustomerId(customerId);
            settlementItems.setIsFullRentCollected(isCustomRent);
            settlementItems.setFullRent(isCustomRent ? customRentAmount : 0);
            settlementItems.setCurrentMonthPayableAmount(currentMonthTotalAmount);
            settlementItems.setCurrentMonthPaidAmount(currentMonthPaidRent);
            settlementItems.setBookingBalance(bookingAvailableBalance);
            settlementItems.setAdvanceBalance(advanceAvailableBalance);

            if (advanceInvoice != null) {
                invoiceV1Service.save(advanceInvoice);
            }
            invoiceV1Service.saveAll(cancelledInvoices);

            return new ResponseEntity<>("Settlement generated successfully", HttpStatus.OK);
        }

        return new ResponseEntity<>("Generate settlement failed", HttpStatus.BAD_REQUEST);
    }

    private CustomerSettlementInfoRes buildFixedDateBasedPrepaidBedChangeSettlementInfo(Customers customer, BookingsV1 booking,
                                                                                        Date leavingDate, HostelV1 hostel,
                                                                                        BillingDates billingDates,
                                                                                        CustomersBedHistory latestBedHistory,
                                                                                        Map<Integer, Floors> floorsMap,
                                                                                        Map<Integer, Rooms> roomsMap,
                                                                                        Map<Integer, Beds> bedsMap,
                                                                                        InvoicesV1 bookingInvoice,
                                                                                        InvoicesV1 advanceInvoice) {

        if (customer == null){
            return null;
        }

        CustomerDeductionsInfoRes customerDeductionsInfoRes = buildDeductionsInfoRes(advanceInvoice);

        CustomerInfoRes customerInfoRes = buildCustomerInfoRes(bookingInvoice, advanceInvoice, booking,
                customer, hostel, latestBedHistory, floorsMap, roomsMap, bedsMap);

        CustomerStayInfoRes customerStayInfoRes = buildCustomerStayInfoRes(booking, leavingDate);

        CustomerEbInfoRes customerEbInfoRes = buildEbInfoRes(hostel, customer, leavingDate,
                floorsMap, roomsMap, bedsMap);

        UnpaidInvoicesInfoRes unpaidInvoicesInfoRes = buildUnpaidInvoiceInfoRes(billingDates, customer);

        CustomerRentInfoRes customerRentInfoRes = buildRentInfoRes(billingDates, customer, leavingDate,
                booking, floorsMap, roomsMap, bedsMap);

        CustomerWalletInfoRes customerWalletInfoRes = buildWalletInfoRes(customer);

        CustomerBookingInfoRes customerBookingInfoRes = buildBookingInfoRes(bookingInvoice);

        CustomerAdvanceInfoRes customerAdvanceInfoRes = buildAdvanceInfoRes(advanceInvoice);

        CustomerFinalSettlementInfoRes finalSettlementInfoRes = buildFinalSettlementInfoRes(customerEbInfoRes,
                customerWalletInfoRes, unpaidInvoicesInfoRes, customerRentInfoRes, customerDeductionsInfoRes,
                customerInfoRes);

        return new CustomerSettlementInfoRes(customerInfoRes, customerStayInfoRes, customerEbInfoRes,
                unpaidInvoicesInfoRes, customerRentInfoRes, customerWalletInfoRes, customerBookingInfoRes,
                customerAdvanceInfoRes, customerDeductionsInfoRes, finalSettlementInfoRes);
    }

    private CustomerSettlementInfoRes buildFixedDateBasedPrepaidSettlementInfo(Customers customer, BookingsV1 booking,
                                                                               Date leavingDate, HostelV1 hostel,
                                                                               BillingDates billingDates,
                                                                               CustomersBedHistory latestBedHistory,
                                                                               Map<Integer, Floors> floorsMap,
                                                                               Map<Integer, Rooms> roomsMap,
                                                                               Map<Integer, Beds> bedsMap,
                                                                               InvoicesV1 bookingInvoice,
                                                                               InvoicesV1 advanceInvoice) {

        if (customer == null){
            return null;
        }

        CustomerDeductionsInfoRes customerDeductionsInfoRes = buildDeductionsInfoRes(advanceInvoice);

        CustomerInfoRes customerInfoRes = buildCustomerInfoRes(bookingInvoice, advanceInvoice, booking,
                customer, hostel, latestBedHistory, floorsMap, roomsMap, bedsMap);

        CustomerStayInfoRes customerStayInfoRes = buildCustomerStayInfoRes(booking, leavingDate);

        CustomerEbInfoRes customerEbInfoRes = buildEbInfoRes(hostel, customer, leavingDate,
                floorsMap, roomsMap, bedsMap);

        UnpaidInvoicesInfoRes unpaidInvoicesInfoRes = buildUnpaidInvoiceInfoRes(billingDates, customer);

        CustomerRentInfoRes customerRentInfoRes = buildRentInfoRes(billingDates, customer, leavingDate,
                booking, floorsMap, roomsMap, bedsMap);

        CustomerWalletInfoRes customerWalletInfoRes = buildWalletInfoRes(customer);

        CustomerBookingInfoRes customerBookingInfoRes = buildBookingInfoRes(bookingInvoice);

        CustomerAdvanceInfoRes customerAdvanceInfoRes = buildAdvanceInfoRes(advanceInvoice);

        CustomerFinalSettlementInfoRes finalSettlementInfoRes = buildFinalSettlementInfoRes(customerEbInfoRes,
                customerWalletInfoRes, unpaidInvoicesInfoRes, customerRentInfoRes, customerDeductionsInfoRes,
                customerInfoRes);

        return new CustomerSettlementInfoRes(customerInfoRes, customerStayInfoRes, customerEbInfoRes,
                unpaidInvoicesInfoRes, customerRentInfoRes, customerWalletInfoRes, customerBookingInfoRes,
                customerAdvanceInfoRes, customerDeductionsInfoRes, finalSettlementInfoRes);
    }

    private CustomerSettlementInfoRes buildFixedDateBasedPostpaidSettlementInfo(Customers customer, BookingsV1 booking,
                                                                                Date leavingDate, HostelV1 hostel,
                                                                                BillingDates billingDates,
                                                                                CustomersBedHistory latestBedHistory,
                                                                                Map<Integer, Floors> floorsMap,
                                                                                Map<Integer, Rooms> roomsMap,
                                                                                Map<Integer, Beds> bedsMap,
                                                                                InvoicesV1 bookingInvoice,
                                                                                InvoicesV1 advanceInvoice) {

        if (customer == null){
            return null;
        }

        CustomerDeductionsInfoRes customerDeductionsInfoRes = buildDeductionsInfoRes(advanceInvoice);

        CustomerInfoRes customerInfoRes = buildCustomerInfoRes(bookingInvoice, advanceInvoice, booking,
                customer, hostel, latestBedHistory, floorsMap, roomsMap, bedsMap);

        CustomerStayInfoRes customerStayInfoRes = buildCustomerStayInfoRes(booking, leavingDate);

        CustomerEbInfoRes customerEbInfoRes = buildEbInfoRes(hostel, customer, leavingDate,
                floorsMap, roomsMap, bedsMap);

        UnpaidInvoicesInfoRes unpaidInvoicesInfoRes = buildUnpaidInvoiceInfoRes(billingDates, customer);

        CustomerRentInfoRes customerRentInfoRes = buildPostpaidRentInfoRes(billingDates, customer,
                leavingDate, booking, floorsMap, roomsMap, bedsMap);

        CustomerWalletInfoRes customerWalletInfoRes = buildWalletInfoRes(customer);

        CustomerBookingInfoRes customerBookingInfoRes = buildBookingInfoRes(bookingInvoice);

        CustomerAdvanceInfoRes customerAdvanceInfoRes = buildAdvanceInfoRes(advanceInvoice);

        CustomerFinalSettlementInfoRes finalSettlementInfoRes = buildFinalSettlementInfoRes(customerEbInfoRes,
                customerWalletInfoRes, unpaidInvoicesInfoRes, customerRentInfoRes, customerDeductionsInfoRes,
                customerInfoRes);

        return new CustomerSettlementInfoRes(customerInfoRes, customerStayInfoRes, customerEbInfoRes,
                unpaidInvoicesInfoRes, customerRentInfoRes, customerWalletInfoRes, customerBookingInfoRes,
                customerAdvanceInfoRes, customerDeductionsInfoRes, finalSettlementInfoRes);
    }

    private CustomerSettlementInfoRes buildJoiningBasedPrepaidSettlementInfo(Customers customer,
                                                                             BookingsV1 booking,
                                                                             Date leavingDate,
                                                                             HostelV1 hostel,
                                                                             BillingDates billingDates,
                                                                             CustomersBedHistory latestBedHistory,
                                                                             Map<Integer, Floors> floorsMap,
                                                                             Map<Integer, Rooms> roomsMap,
                                                                             Map<Integer, Beds> bedsMap,
                                                                             InvoicesV1 bookingInvoice,
                                                                             InvoicesV1 advanceInvoice) {

        if (customer == null){
            return null;
        }

        CustomerDeductionsInfoRes customerDeductionsInfoRes = buildDeductionsInfoRes(advanceInvoice);

        CustomerInfoRes customerInfoRes = buildCustomerInfoRes(bookingInvoice, advanceInvoice, booking,
                customer, hostel, latestBedHistory, floorsMap, roomsMap, bedsMap);

        CustomerStayInfoRes customerStayInfoRes = buildCustomerStayInfoRes(booking, leavingDate);

        CustomerEbInfoRes customerEbInfoRes = buildEbInfoRes(hostel, customer, leavingDate,
                floorsMap, roomsMap, bedsMap);

        UnpaidInvoicesInfoRes unpaidInvoicesInfoRes = buildUnpaidInvoiceInfoRes(billingDates, customer);

        CustomerRentInfoRes customerRentInfoRes = buildRentInfoRes(billingDates, customer, leavingDate,
                booking, floorsMap, roomsMap, bedsMap);

        CustomerWalletInfoRes customerWalletInfoRes = buildWalletInfoRes(customer);

        CustomerBookingInfoRes customerBookingInfoRes = buildBookingInfoRes(bookingInvoice);

        CustomerAdvanceInfoRes customerAdvanceInfoRes = buildAdvanceInfoRes(advanceInvoice);

        CustomerFinalSettlementInfoRes finalSettlementInfoRes = buildFinalSettlementInfoRes(customerEbInfoRes,
                customerWalletInfoRes, unpaidInvoicesInfoRes, customerRentInfoRes, customerDeductionsInfoRes,
                customerInfoRes);

        return new CustomerSettlementInfoRes(customerInfoRes, customerStayInfoRes, customerEbInfoRes,
                unpaidInvoicesInfoRes, customerRentInfoRes, customerWalletInfoRes, customerBookingInfoRes,
                customerAdvanceInfoRes, customerDeductionsInfoRes, finalSettlementInfoRes);
    }

    private CustomerFinalSettlementInfoRes buildFinalSettlementInfoRes(CustomerEbInfoRes customerEbInfoRes,
                                                                       CustomerWalletInfoRes customerWalletInfoRes,
                                                                       UnpaidInvoicesInfoRes unpaidInvoicesInfoRes,
                                                                       CustomerRentInfoRes customerRentInfoRes,
                                                                       CustomerDeductionsInfoRes customerDeductionsInfoRes,
                                                                       CustomerInfoRes customerInfoRes) {

        boolean isBookingOrAdvancePaid = false;
        double availableAmountToRedeem = 0;

        if (customerInfoRes != null){
            isBookingOrAdvancePaid = customerInfoRes.isBookingOrAdvancePaid();
            if (customerInfoRes.availableRedemptionAmount() != null) {
                AvailableRedemptionAmountRes availableRedemptionAmountRes = customerInfoRes.availableRedemptionAmount();
                if (availableRedemptionAmountRes.totalAvailableAmountToRedeem() != null){
                    availableAmountToRedeem = availableRedemptionAmountRes.totalAvailableAmountToRedeem();
                }
            }
        }

        double pendingDeductionAmount = 0.0;
        if (customerDeductionsInfoRes != null){
            if (customerDeductionsInfoRes.pendingAmount() != null){
                pendingDeductionAmount = customerDeductionsInfoRes.pendingAmount();
            }
        }

        double ebAmount = 0.0;
        if (customerEbInfoRes != null) {
            ebAmount = customerEbInfoRes.pendingEbAmount() != null ?
                    customerEbInfoRes.pendingEbAmount() : 0.0;
        }

        double unpaidInvoicesTotalAmount = 0;
        double unpaidInvoicesPaidAmount = 0;
        double unpaidInvoicesUnPaidAmount = 0;

        if (unpaidInvoicesInfoRes != null) {
            unpaidInvoicesTotalAmount = unpaidInvoicesInfoRes.invoiceTotalAmount();
            unpaidInvoicesPaidAmount = unpaidInvoicesInfoRes.paidAmount();
            unpaidInvoicesUnPaidAmount = unpaidInvoicesInfoRes.unpaidAmount();
        }

        double currentMonthTotalAmount = 0;
        double currentRentPaidAmount = 0;
        double currentMonthPendingAmount = 0;
        String label = null;
        double pendingAmount = 0.0;
        double totalRefundableRent = 0.0;
        double discountAmount = 0.0;
        double currentPayableRent = 0.0;
        double fullRent = 0.0;
        double otherItemAmount = 0.0;

        if (customerRentInfoRes != null) {
            currentMonthTotalAmount = customerRentInfoRes.currentMonthTotalAmount() != null ?
                    customerRentInfoRes.currentMonthTotalAmount() : 0.0;
            currentRentPaidAmount = customerRentInfoRes.currentRentPaid() != null ?
                    customerRentInfoRes.currentRentPaid() : 0.0;
            currentMonthPendingAmount = customerRentInfoRes.currentMonthPendingAmount() != null ?
                    customerRentInfoRes.currentMonthPendingAmount() : 0.0;
            discountAmount = customerRentInfoRes.discountAmount() != null ?
                    customerRentInfoRes.discountAmount() : 0.0;
            currentPayableRent = customerRentInfoRes.currentPayableRent() != null ?
                    customerRentInfoRes.currentPayableRent() : 0.0;
            fullRent = customerRentInfoRes.fullRent() != null ?
                    customerRentInfoRes.fullRent() : 0.0;
            otherItemAmount = customerRentInfoRes.otherItemAmount() != null ?
                    customerRentInfoRes.otherItemAmount() : 0.0;

            if (currentRentPaidAmount > currentPayableRent) {
                label = "Refundable rent";
                pendingAmount = currentMonthPendingAmount;
                totalRefundableRent = currentMonthPendingAmount;
            } else {
                label = "Payable rent";
                pendingAmount = currentMonthPendingAmount;
            }
        }

        double walletAmount = 0;
        if (customerWalletInfoRes != null) {
            walletAmount = customerWalletInfoRes.walletAmount() != null ?
                    customerWalletInfoRes.walletAmount() : 0.0;
        }

        double totalRefundableAdvance = 0.0;
        double totalAmountToBePaid = unpaidInvoicesTotalAmount + ebAmount +
                walletAmount + currentMonthTotalAmount;
        double paidAmount = unpaidInvoicesPaidAmount + currentRentPaidAmount;
        double pendingRent = unpaidInvoicesUnPaidAmount + currentMonthPendingAmount;

        totalAmountToBePaid = totalAmountToBePaid - paidAmount;
        totalAmountToBePaid = totalAmountToBePaid + pendingDeductionAmount;
        if (isBookingOrAdvancePaid) {
            totalAmountToBePaid = totalAmountToBePaid - availableAmountToRedeem;
            totalRefundableAdvance = availableAmountToRedeem;
        }
        boolean isRefundable = totalAmountToBePaid < 0;

        return new CustomerFinalSettlementInfoRes(label,
                Utils.roundOfDoubleTo2Digits(totalAmountToBePaid), Utils.roundOfDoubleTo2Digits(fullRent),
                Utils.roundOfDoubleTo2Digits(unpaidInvoicesUnPaidAmount), Utils.roundOfDoubleTo2Digits(otherItemAmount),
                Utils.roundOfDoubleTo2Digits(pendingRent), Utils.roundOfDoubleTo2Digits(currentPayableRent),
                Utils.roundOfDoubleTo2Digits(currentRentPaidAmount), Utils.roundOfDoubleTo2Digits(pendingAmount),
                Utils.roundOfDoubleTo2Digits(pendingDeductionAmount), Utils.roundOfDoubleTo2Digits(ebAmount),
                Utils.roundOfDoubleTo2Digits(walletAmount), Utils.roundOfDoubleTo2Digits(discountAmount),
                Utils.roundOfDoubleTo2Digits(totalRefundableAdvance), isRefundable,
                Utils.roundOfDoubleTo2Digits(totalRefundableRent)
        );
    }

    private CustomerInfoRes buildCustomerInfoRes(InvoicesV1 bookingInvoice, InvoicesV1 advanceInvoice,
                                                 BookingsV1 booking, Customers customer, HostelV1 hostel,
                                                 CustomersBedHistory latestBedHistory,
                                                 Map<Integer, Floors> floorsMap,
                                                 Map<Integer, Rooms> roomsMap,
                                                 Map<Integer, Beds> bedsMap) {

        if (customer == null || hostel == null){
            return null;
        }

        String customerId = customer.getCustomerId();
        boolean isBookingOrAdvancePaid = false;
        double totalBookingAndAdvancePaidAmount = 0.0;
        double bookingInvoicePaidAmount = 0.0;
        double availableBookingAmountToRedeem = 0.0;
        double availableAdvanceAmountToRedeem = 0.0;
        double availableAmountToRedeem = 0.0;
        double advanceAmountRedeemedFromBookingInvoice = 0.0;

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

        Integer floorId = null;
        String floorName = null;
        Integer roomId = null;
        String roomName = null;
        Integer bedId = null;
        String bedName = null;
        if (latestBedHistory != null) {
            floorId = latestBedHistory.getFloorId();
            roomId = latestBedHistory.getRoomId();
            bedId = latestBedHistory.getBedId();
            Floors floor = floorsMap.getOrDefault(floorId, null);
            Rooms room = roomsMap.getOrDefault(roomId, null);
            if (room != null) {
                roomName = room.getRoomName();
                floorId = room.getFloorId();
                floor = floorsMap.getOrDefault(floorId, null);
            }
            Beds bed = bedsMap.getOrDefault(bedId, null);
            if (bed != null) {
                bedName = bed.getBedName();
            }
            if (floor != null) {
                floorName = floor.getFloorName();
            }
        }

        AvailableRedemptionAmountRes availableRedemptionAmountRes = new AvailableRedemptionAmountRes(
                availableBookingAmountToRedeem, availableAdvanceAmountToRedeem, availableAmountToRedeem);

        return new CustomerInfoRes(customerId, customer.getFirstName(), customer.getLastName(),
                Utils.getFullName(customer.getFirstName(), customer.getLastName()), customer.getHostelId(),
                hostel.getHostelName(), floorId, floorName, roomId, roomName, bedId, bedName, customer.getProfilePic(),
                Utils.getInitials(customer.getFirstName(), customer.getLastName()), "91", customer.getMobile(),
                joiningDate, customerAdvanceAmount, bookingRentAmount, isBookingOrAdvancePaid, totalBookingAndAdvancePaidAmount,
                bookingInvoicePaidAmount, availableRedemptionAmountRes);
    }

    private CustomerDeductionsInfoRes buildDeductionsInfoRes(InvoicesV1 advanceInvoice) {

        CustomerDeductionsInfoRes customerDeductionsInfoRes = null;

        if (advanceInvoice == null){
            return null;
        }

        if (advanceInvoice.getDeductions() != null &&
                advanceInvoice.getDeductionAmount() != null &&
                advanceInvoice.getDeductionAmount() > 0) {

            double totalDeductionAmount = 0.0;
            double paidDeductionAmount = 0.0;
            double pendingDeductionAmount = 0.0;

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

                customerDeductionsInfoRes = new CustomerDeductionsInfoRes(totalDeductionAmount,
                        paidDeductionAmount, pendingDeductionAmount, deductionsInfo);
            }
        }

        return customerDeductionsInfoRes;
    }

    private CustomerStayInfoRes buildCustomerStayInfoRes(BookingsV1 booking, Date leavingDate) {

        String bookedDate = null;
        String noticeDate = null;
        String requestedLeavingDate = null;
        String actualLeavingDate = null;

        if (leavingDate != null){
            actualLeavingDate = Utils.dateToString(leavingDate);
        }

        if (booking != null){
            bookedDate = Utils.dateToString(booking.getBookingDate());
            noticeDate = Utils.dateToString(booking.getNoticeDate());
            requestedLeavingDate = Utils.dateToString(booking.getLeavingDate());
        }

        return new CustomerStayInfoRes(bookedDate, noticeDate,
                requestedLeavingDate, actualLeavingDate);
    }

    private CustomerRentInfoRes buildRentInfoRes(BillingDates billingDates, Customers customer,
                                                 Date leavingDate, BookingsV1 booking,
                                                 Map<Integer, Floors> floorsMap,
                                                 Map<Integer, Rooms> roomsMap,
                                                 Map<Integer, Beds> bedsMap) {

        CustomerRentInfoRes customerRentInfoRes = null;

        if (customer == null){
            return null;
        }

        String customerId = customer.getCustomerId();
        String hostelId = customer.getHostelId();

        if (billingDates != null && billingDates.currentBillStartDate() != null
                && billingDates.currentBillEndDate() != null) {

            List<InvoicesV1> currentMonthInvoices = invoiceV1Service
                    .getCurrentMonthInvoices(customerId, hostelId, billingDates.currentBillStartDate());

            if (!currentMonthInvoices.isEmpty()) {

                // latest invoice
                InvoicesV1 latestCurrentMonthInvoice = currentMonthInvoices.stream()
                        .max(Comparator.comparing(InvoicesV1::getInvoiceStartDate))
                        .orElse(null);

                Date currentMonthStartDate = customer.getJoiningDate() != null
                        && Utils.compareWithTwoDates(customer.getJoiningDate(), billingDates.currentBillStartDate()) >= 0
                        ? customer.getJoiningDate()
                        : billingDates.currentBillStartDate();

                boolean discountApplied = false;
                double discountAmount = 0.0;

                double otherItemAmount = 0.0;
                List<OtherItemsRes> otherItems = new ArrayList<>();
                Set<String> discountedInvoiceIds = new HashSet<>();

                for (InvoicesV1 invoice : currentMonthInvoices) {

                    if (invoice.isDiscounted()) {
                        discountApplied = true;
                        discountedInvoiceIds.add(invoice.getInvoiceId());
                    }

                    if (invoice.getInvoiceItems() == null) {
                        continue;
                    }

                    for (InvoiceItems item : invoice.getInvoiceItems()) {

                        if (com.smartstay.console.ennum.InvoiceItems.RENT.name()
                                .equals(item.getInvoiceItem())) {
                            continue;
                        }

                        if (item.getAmount() != null) {
                            otherItemAmount += item.getAmount();
                        }

                        String itemName;

                        switch (item.getInvoiceItem()) {
                            case "OTHERS" -> itemName = item.getOtherItem();
                            case "EB" -> itemName = "Electricity";
                            case "AMENITY" -> itemName = "Amenities";
                            default -> itemName = item.getInvoiceItem();
                        }

                        otherItems.add(new OtherItemsRes(itemName, item.getAmount()));
                    }
                }

                if (!discountedInvoiceIds.isEmpty()) {
                    discountAmount = invoiceDiscountsService
                            .getByHostelIdAndInvoiceIds(hostelId, discountedInvoiceIds)
                            .stream()
                            .mapToDouble(InvoiceDiscounts::getDiscountAmount)
                            .sum();
                }

                // Previous invoices
                List<InvoicesV1> oldInvoices = currentMonthInvoices.stream()
                        .filter(i -> !i.getInvoiceId().equals(latestCurrentMonthInvoice.getInvoiceId()))
                        .toList();

                double payableRent = oldInvoices.stream()
                        .mapToDouble(i -> i.getTotalAmount() == null ? 0 : i.getTotalAmount())
                        .sum();

                double paidRent = oldInvoices.stream()
                        .mapToDouble(i -> i.getPaidAmount() == null ? 0 : i.getPaidAmount())
                        .sum();

                double runningInvoicePaid = latestCurrentMonthInvoice.getPaidAmount() == null
                        ? 0
                        : latestCurrentMonthInvoice.getPaidAmount();

                long daysInMonth = Utils.getLastDayOfMonth(billingDates.currentBillStartDate());

                double monthlyRent = 0;
                if (booking != null){
                    monthlyRent = booking.getRentAmount() != null ? booking.getRentAmount() : 0;
                }
                double rentPerDay = monthlyRent / daysInMonth;

                long stayedDays = Utils
                        .findNumberOfDays(latestCurrentMonthInvoice.getInvoiceStartDate(), leavingDate);

                double runningInvoiceRent = rentPerDay * stayedDays;

                payableRent += runningInvoiceRent;
                paidRent += runningInvoicePaid;

                double currentMonthTotalAmount = payableRent + otherItemAmount;
                double currentMonthPendingAmount = currentMonthTotalAmount - paidRent;

                // Rent breakup
                List<CustomersBedHistory> customersBedHistories = customerBedHistoryService
                        .findBedHistoriesByCustomerIdAndDates(customerId, billingDates.currentBillStartDate(),
                                billingDates.currentBillEndDate());

                List<RentBreakUpInfoRes> rentBreakUpInfoRes = buildRentBreakUpInfoRes(customersBedHistories,
                        bedsMap, roomsMap, floorsMap, billingDates, leavingDate);

                double fullRent = 0;
                long totalStayDays = 0;

                if (!rentBreakUpInfoRes.isEmpty()) {

                    totalStayDays = rentBreakUpInfoRes.stream()
                            .mapToLong(RentBreakUpInfoRes::noOfDays)
                            .sum();

                    fullRent = rentBreakUpInfoRes.stream()
                            .mapToDouble(RentBreakUpInfoRes::rent)
                            .max()
                            .orElse(0);
                }

                double currentMonthRentOnly = currentMonthTotalAmount - otherItemAmount;
                double rentDifference = fullRent - currentMonthRentOnly;

                if (paidRent > 0) {
                    if (paidRent >= fullRent + otherItemAmount) {
                        rentDifference = currentMonthPendingAmount * -1;
                    } else if (paidRent > currentMonthTotalAmount) {
                        if (paidRent > fullRent) {
                            double diff = (paidRent - otherItemAmount) - currentMonthRentOnly;
                            rentDifference -= diff;
                        }
                    }
                }

                customerRentInfoRes = new CustomerRentInfoRes(
                        Utils.roundOfDoubleTo2Digits(payableRent),
                        Utils.roundOfDoubleTo2Digits(paidRent),
                        (int) totalStayDays,
                        Utils.roundOfDoubleTo2Digits(monthlyRent),
                        Utils.roundOfDoubleTo2Digits(currentMonthTotalAmount),
                        Utils.roundOfDoubleTo2Digits(currentMonthPendingAmount),
                        Utils.dateToString(currentMonthStartDate),
                        Utils.dateToString(billingDates.currentBillEndDate()),
                        latestCurrentMonthInvoice.getInvoiceId(),
                        Utils.roundOfDoubleTo2Digits(otherItemAmount),
                        discountApplied,
                        Utils.roundOfDoubleTo2Digits(discountAmount),
                        Utils.roundOfDoubleTo2Digits(fullRent),
                        Utils.roundOfDoubleTo2Digits(rentDifference),
                        otherItems,
                        rentBreakUpInfoRes
                );
            }
        }

        return customerRentInfoRes;
    }

    private CustomerRentInfoRes buildPostpaidRentInfoRes(BillingDates billingDates, Customers customer,
                                                         Date leavingDate, BookingsV1 booking,
                                                         Map<Integer, Floors> floorsMap,
                                                         Map<Integer, Rooms> roomsMap,
                                                         Map<Integer, Beds> bedsMap){

        CustomerRentInfoRes customerRentInfoRes = null;

        if (customer == null){
            return null;
        }

        String customerId = customer.getCustomerId();

        if (billingDates != null && billingDates.currentBillStartDate() != null
                && billingDates.currentBillEndDate() != null && leavingDate != null) {

            // Rent breakup
            List<CustomersBedHistory> customersBedHistories = customerBedHistoryService
                    .getCustomerHistoriesByCustomerIdAndEndDateBefore(customerId, billingDates.currentBillStartDate());

            List<RentBreakUpInfoRes> rentBreakUpInfoRes = buildRentBreakUpInfoRes(customersBedHistories,
                    bedsMap, roomsMap, floorsMap, billingDates, leavingDate);

            List<OtherItemsRes> otherItems = new ArrayList<>();

            Date startDate = null;
            if (booking.getJoiningDate() != null){
                if (Utils.compareWithTwoDates(booking.getJoiningDate(), billingDates.currentBillStartDate()) < 0) {
                    startDate = billingDates.currentBillStartDate();
                } else {
                    startDate = booking.getJoiningDate();
                }
            }

            if (startDate == null){
                throw new BadRequestException(Utils.DATE_IS_NULL);
            }

            List<CustomersAmenity> customersAmenities = customersAmenityService
                    .getAllByCustomerIdAndDatesBetween(customerId, startDate, leavingDate);

            Set<String> amenityIds = customersAmenities.stream()
                    .map(CustomersAmenity::getAmenityId)
                    .collect(Collectors.toSet());

            List<AmenitiesV1> amenities = amenitiesService.getAmenitiesByIds(amenityIds);

            Map<String, AmenitiesV1> amenitiesMap = amenities.stream()
                    .collect(Collectors.toMap(AmenitiesV1::getAmenityId, Function.identity()));

            double otherItemAmount = 0.0;
            long totalStayDays = Utils.findNumberOfDays(startDate, leavingDate);
            long totalDaysInMonth = Utils.findNumberOfDays(
                    billingDates.currentBillStartDate(), billingDates.currentBillEndDate());

            for (CustomersAmenity customerAmenity : customersAmenities) {

                AmenitiesV1 amenity = amenitiesMap.get(customerAmenity.getAmenityId());
                if (amenity == null) {
                    continue;
                }

                double amount;

                if (amenity.getIsProRate()) {
                    double perDayAmount = amenity.getAmenityAmount() / totalDaysInMonth;
                    amount = perDayAmount * totalStayDays;
                } else {
                    amount = amenity.getAmenityAmount();
                }

                otherItemAmount += amount;

                otherItems.add(new OtherItemsRes(amenity.getAmenityName(), Utils.roundOfDoubleTo2Digits(amount)));
            }

            double payableRent = rentBreakUpInfoRes.stream()
                    .mapToDouble(RentBreakUpInfoRes::totalRent)
                    .sum();

            long stayDays = rentBreakUpInfoRes.stream()
                    .mapToLong(RentBreakUpInfoRes::noOfDays)
                    .sum();

            double monthlyRent = booking.getRentAmount();

            double fullRent = 0.0;
            if (!rentBreakUpInfoRes.isEmpty()) {
                fullRent = rentBreakUpInfoRes.stream()
                        .map(RentBreakUpInfoRes::rent)
                        .max(Double::compareTo)
                        .orElse(0.0);
            }

            double currentMonthTotalAmount = payableRent + otherItemAmount;
            double currentMonthPendingAmount = currentMonthTotalAmount;
            double rentDifference = fullRent - payableRent;

            Date currentMonthStartDate;
            if (booking.getJoiningDate() != null &&
                    Utils.compareWithTwoDates(booking.getJoiningDate(),
                            billingDates.currentBillStartDate()) < 0) {
                currentMonthStartDate = billingDates.currentBillStartDate();
            } else {
                currentMonthStartDate = booking.getJoiningDate();
            }

            customerRentInfoRes = new CustomerRentInfoRes(
                    Utils.roundOfDoubleTo2Digits(payableRent),
                    0.0,
                    (int) stayDays,
                    Utils.roundOfDoubleTo2Digits(monthlyRent),
                    Utils.roundOfDoubleTo2Digits(currentMonthTotalAmount),
                    Utils.roundOfDoubleTo2Digits(currentMonthPendingAmount),
                    Utils.dateToString(currentMonthStartDate),
                    Utils.dateToString(billingDates.currentBillEndDate()),
                    null,
                    Utils.roundOfDoubleTo2Digits(otherItemAmount),
                    false,
                    0.0,
                    Utils.roundOfDoubleTo2Digits(fullRent),
                    Utils.roundOfDoubleTo2Digits(rentDifference),
                    otherItems,
                    rentBreakUpInfoRes
            );
        }

        return customerRentInfoRes;
    }

    private List<RentBreakUpInfoRes> buildRentBreakUpInfoRes(List<CustomersBedHistory> customersBedHistories,
                                                             Map<Integer, Beds> bedsMap,
                                                             Map<Integer, Rooms> roomsMap,
                                                             Map<Integer, Floors> floorsMap,
                                                             BillingDates billingDates,
                                                             Date leavingDate) {

        if (billingDates == null || leavingDate == null) {
            return new ArrayList<>();
        }

        return customersBedHistories.stream()
                .map(bedHistory -> {

                    Date dbStartDate = null;
                    Date dbEndDate = null;
                    String startDate = null;
                    String endDate = null;
                    long noOfDays = 0;
                    double bedHistoryRentPerDay = 0;
                    double rent = 0;
                    double totalRent = 0;
                    String bedName = null;
                    String roomName = null;
                    String floorName = null;

                    if (Utils.compareWithTwoDates(bedHistory.getStartDate(), billingDates.currentBillStartDate()) < 0){
                        dbStartDate = billingDates.currentBillStartDate();
                    } else {
                        dbStartDate = bedHistory.getStartDate();
                    }

                    if (bedHistory.getEndDate() == null) {
                        dbEndDate = leavingDate;
                    } else {
                        dbEndDate = bedHistory.getEndDate();
                    }

                    if (dbStartDate != null){
                        startDate = Utils.dateToString(dbStartDate);
                    }
                    if (dbEndDate != null){
                        endDate = Utils.dateToString(dbEndDate);
                    }

                    if (dbStartDate != null && dbEndDate != null){
                        noOfDays = Utils.findNumberOfDays(dbStartDate, dbEndDate);
                    }

                    long currentBillingCycleDays = Utils.findNumberOfDays(
                            billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
                    bedHistoryRentPerDay = bedHistory.getRentAmount() / currentBillingCycleDays;
                    totalRent = bedHistoryRentPerDay * noOfDays;
                    rent =  bedHistory.getRentAmount();

                    Rooms room = roomsMap.getOrDefault(bedHistory.getRoomId(), null);
                    Beds bed = bedsMap.getOrDefault(bedHistory.getBedId(), null);

                    Integer floorId = bedHistory.getFloorId();
                    Floors floor = floorsMap.getOrDefault(floorId, null);

                    if (room != null) {
                        floorId = room.getFloorId();
                        floor = floorsMap.getOrDefault(floorId, null);
                    }

                    floorName = floor != null ? floor.getFloorName() : null;
                    roomName = room != null ? room.getRoomName() : null;
                    bedName = bed != null ? bed.getBedName() : null;

                    return new RentBreakUpInfoRes(dbStartDate, dbEndDate, startDate, endDate,
                            noOfDays, Utils.roundOfDoubleTo2Digits(bedHistoryRentPerDay),
                            Utils.roundOfDoubleTo2Digits(rent), Utils.roundOfDoubleTo2Digits(totalRent),
                            bedHistory.getBedId(), bedName, bedHistory.getRoomId(), roomName,
                            floorId, floorName);
                }).toList();
    }

    private CustomerEbInfoRes buildEbInfoRes(HostelV1 hostel, Customers customer, Date leavingDate,
                                             Map<Integer, Floors> floorsMap,
                                             Map<Integer, Rooms> roomsMap,
                                             Map<Integer, Beds> bedsMap) {

        CustomerEbInfoRes customerEbInfoRes = null;

        if (customer == null) {
            return null;
        }

        String customerId = customer.getCustomerId();

        String hostelId;
        ElectricityConfig ebConfig;
        if (hostel != null){
            ebConfig = hostel.getElectricityConfig();
            hostelId = hostel.getHostelId();
        } else {
            ebConfig = null;
            hostelId = null;
        }

        if (ebConfig != null && leavingDate != null && hostelId != null){

            List<MissedEbRoomsRes> allMissedEbRoomsRes = new ArrayList<>();
            List<PendingEbRes> allPendingEbRes = new ArrayList<>();

            if (EBReadingType.ROOM_READING.name().equals(ebConfig.getTypeOfReading())){

                List<ElectricityReadings> allPendingEbReadings = new ArrayList<>();

                List<CustomersBedHistory> customersBedHistories = customerBedHistoryService
                        .getBedHistoriesByCustomerIdAndTypeNotIn(customerId, CustomersBedType.BOOKED.name());

                Map<Integer, List<CustomersBedHistory>> customerRoomHistoryMap = customersBedHistories.stream()
                        .collect(Collectors.groupingBy(CustomersBedHistory::getRoomId));

                Set<Integer> roomIds = customersBedHistories
                        .stream()
                        .map(CustomersBedHistory::getRoomId)
                        .collect(Collectors.toSet());

                List<CustomersBedHistory> roomBedHistories = customerBedHistoryService
                        .getBedHistoriesByRoomIdsAndTypeNotIn(roomIds, CustomersBedType.BOOKED.name());

                Map<Integer, List<CustomersBedHistory>> roomHistoryMap = roomBedHistories.stream()
                                .collect(Collectors.groupingBy(CustomersBedHistory::getRoomId));

                List<ElectricityReadings> latestReadingOfRooms = electricityReadingsService
                        .getLatestEntriesByHostelIdAndRoomIds(hostelId, roomIds);

                customerRoomHistoryMap.forEach((roomId, histories) -> {

                    Date start = histories.stream()
                            .map(CustomersBedHistory::getStartDate)
                            .filter(Objects::nonNull)
                            .min(Date::compareTo)
                            .orElse(null);

                    Date end = histories.stream()
                            .map(h -> h.getEndDate() == null ? leavingDate : h.getEndDate())
                            .max(Date::compareTo)
                            .orElse(leavingDate);

                    List<ElectricityReadings> readings = electricityReadingsService
                            .getPendingReadingsBetweenDates(hostelId, roomId, start, end);

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
                        List<PendingEbRes> ebHistoryResponses = buildPendingEbResponseByEbHistory(
                                pendingCustomersEbHistories, customersBedHistories, floorsMap, roomsMap,
                                bedsMap, leavingDate);

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
                                    buildPendingEbResponse(readingsToCalculate, roomHistoryMap,
                                            floorsMap, roomsMap, bedsMap, customerId, leavingDate, ebConfig)
                            );
                        }
                    } else {

                        allPendingEbRes.addAll(
                                buildPendingEbResponse(uniquePendingReadings, roomHistoryMap,
                                        floorsMap, roomsMap, bedsMap, customerId, leavingDate, ebConfig)
                        );
                    }
                }

                allMissedEbRoomsRes.addAll(
                        buildMissedEbRoomResponse(customersBedHistories, latestReadingOfRooms,
                                floorsMap, roomsMap, bedsMap, leavingDate)
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

            allPendingEbRes.sort(
                    Comparator
                            .comparing(
                                    PendingEbRes::dbFromDate,
                                    Comparator.nullsLast(Date::compareTo)
                            )
                            .thenComparing(
                                    PendingEbRes::dbEndDate,
                                    Comparator.nullsLast(Date::compareTo)
                            )
                            .thenComparing(
                                    PendingEbRes::bedId,
                                    Comparator.nullsLast(Integer::compareTo)
                            )
            );

            allMissedEbRoomsRes.sort(
                    Comparator
                            .comparing(
                                    MissedEbRoomsRes::dbFromDate,
                                    Comparator.nullsLast(Date::compareTo)
                            )
                            .thenComparing(
                                    MissedEbRoomsRes::dbToDate,
                                    Comparator.nullsLast(Date::compareTo)
                            )
                            .thenComparing(
                                    MissedEbRoomsRes::bedId,
                                    Comparator.nullsLast(Integer::compareTo)
                            )
            );

            customerEbInfoRes = new CustomerEbInfoRes(0.0, ebConfig.getCharge(), "NA",
                    ebConfig.getTypeOfReading(), Utils.roundOfDoubleTo2Digits(pendingEbAmount),
                    false, true, allMissedEbRoomsRes, allPendingEbRes);
        }

        return customerEbInfoRes;
    }

    private UnpaidInvoicesInfoRes buildUnpaidInvoiceInfoRes(BillingDates billingDates,
                                                            Customers customer) {

        if (customer == null){
            return null;
        }
        String customerId = customer.getCustomerId();

        UnpaidInvoicesInfoRes unpaidInvoicesInfoRes = null;

        if (billingDates != null){
            List<InvoicesV1> unpaidInvoices = new ArrayList<>();

            if (billingDates.currentBillStartDate() != null){

                Set<String> invoicesTypes = new HashSet<>();
                invoicesTypes.add(InvoiceType.RENT.name());
                invoicesTypes.add(InvoiceType.REASSIGN_RENT.name());

                unpaidInvoices = invoiceV1Service
                        .getOlderUnpaidInvoicesByInvoiceTypes(customerId, invoicesTypes,
                                billingDates.currentBillStartDate());
            }

            if (unpaidInvoices != null && !unpaidInvoices.isEmpty()) {

                double totalAmount = 0;
                double paidAmount = 0;
                double unpaidAmount = 0;
                for (InvoicesV1 unpaidInvoice : unpaidInvoices) {
                    double invoiceTotalAmount = unpaidInvoice.getTotalAmount() != null ? unpaidInvoice.getTotalAmount() : 0;
                    double invoicePaidAmount = unpaidInvoice.getPaidAmount() != null ? unpaidInvoice.getPaidAmount() : 0;
                    if (PaymentStatus.PENDING.name().equals(unpaidInvoice.getPaymentStatus())) {
                        unpaidAmount += invoiceTotalAmount;
                    } else if (PaymentStatus.PARTIAL_PAYMENT.name().equals(unpaidInvoice.getPaymentStatus())){
                        double pendingAmount = invoiceTotalAmount - invoicePaidAmount;
                        unpaidAmount += pendingAmount;
                        paidAmount += invoicePaidAmount;
                    }
                    totalAmount += invoiceTotalAmount;
                }

                List<UnpaidInvoicesRes> unpaidInvoicesRes = buildUnpaidInvoicesRes(unpaidInvoices);

                unpaidInvoicesInfoRes = new UnpaidInvoicesInfoRes(unpaidInvoices.size(),
                        Utils.roundOfDoubleTo2Digits(totalAmount), Utils.roundOfDoubleTo2Digits(paidAmount),
                        Utils.roundOfDoubleTo2Digits(unpaidAmount), unpaidInvoicesRes);
            }
        }

        return unpaidInvoicesInfoRes;
    }

    private List<UnpaidInvoicesRes> buildUnpaidInvoicesRes(List<InvoicesV1> unpaidInvoices) {
        return unpaidInvoices.stream()
                .map(unpaidInvoice -> {

                    String invoiceType = null;
                    double invoicePendingAmount = 0.0;
                    double ebAmount = 0.0;
                    double amenityAmount = 0.0;

                    double invoiceTotalAmount = unpaidInvoice.getTotalAmount() != null ? unpaidInvoice.getTotalAmount() : 0;
                    double invoicePaidAmount = unpaidInvoice.getPaidAmount() != null ? unpaidInvoice.getPaidAmount() : 0;

                    if (unpaidInvoice.getPaidAmount() != null) {
                        invoicePendingAmount = invoiceTotalAmount - invoicePaidAmount;
                    }

                    ebAmount = unpaidInvoice
                            .getInvoiceItems()
                            .stream()
                            .filter(item ->  com.smartstay.console.ennum.InvoiceItems.EB.name()
                                    .equals(item.getInvoiceItem()))
                            .mapToDouble(InvoiceItems::getAmount)
                            .sum();

                    if (unpaidInvoice.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) ||
                            unpaidInvoice.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
                        invoiceType = "Rent";
                    }
                    else if (unpaidInvoice.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
                        invoiceType = "Advance";
                    }

                    return new UnpaidInvoicesRes(unpaidInvoice.getInvoiceId(),
                            unpaidInvoice.getInvoiceNumber(), invoiceType, unpaidInvoice.getInvoiceType(),
                            Utils.roundOfDoubleTo2Digits(invoiceTotalAmount),
                            Utils.roundOfDoubleTo2Digits(invoicePaidAmount),
                            Utils.roundOfDoubleTo2Digits(invoicePendingAmount),
                            Utils.roundOfDoubleTo2Digits(ebAmount),
                            Utils.roundOfDoubleTo2Digits(amenityAmount));
                }).toList();
    }

    private CustomerWalletInfoRes buildWalletInfoRes(Customers customer) {

        double walletAmount = 0;
        List<WalletHistoryRes> walletHistoryRes = new ArrayList<>();

        CustomerWallet customerWallet = customer.getWallet();

        if (customerWallet != null) {
            walletAmount = customerWallet.getAmount() != null ? customerWallet.getAmount() : 0.0;
        }

        List<CustomerWalletHistory> customerWalletHistories = customerWalletHistoryService
                .getAllInvoiceNotGeneratedWallets(customer.getCustomerId());

        walletHistoryRes = buildWalletHistoryRes(customerWalletHistories);

        return new CustomerWalletInfoRes(Utils.roundOfDoubleTo2Digits(walletAmount), walletHistoryRes);
    }

    private List<WalletHistoryRes> buildWalletHistoryRes(List<CustomerWalletHistory> customerWalletHistories) {

        return customerWalletHistories.stream()
                .map(walletHistory -> {

                    String source = null;
                    String billStartDate = null;
                    String billEndDate = null;

                    if (WalletSource.ELECTRICITY.name().equalsIgnoreCase(walletHistory.getSourceType())) {
                        source = "Electricity";
                    }
                    else if (WalletSource.CHANGE_BED.name().equalsIgnoreCase(walletHistory.getSourceType())) {
                        source = "Change Bed";
                    }
                    else if (WalletSource.AMENITY.name().equalsIgnoreCase(walletHistory.getSourceType())) {
                        source = "Amenity";
                    }

                    if (walletHistory.getBillStartDate() != null) {
                        billStartDate = Utils.dateToString(walletHistory.getBillStartDate());
                    }
                    if (walletHistory.getBillEndDate() != null) {
                        billEndDate = Utils.dateToString(walletHistory.getBillEndDate());
                    }

                    return new WalletHistoryRes(walletHistory.getHistoryId(),
                            Utils.roundOfDoubleTo2Digits(walletHistory.getAmount()), walletHistory.getSourceId(),
                            source, walletHistory.getSourceType(), billStartDate, billEndDate);
                }).toList();
    }

    private CustomerAdvanceInfoRes buildAdvanceInfoRes(InvoicesV1 advanceInvoice) {

        CustomerAdvanceInfoRes customerAdvanceInfoRes = null;

        if (advanceInvoice == null){
            customerAdvanceInfoRes = new CustomerAdvanceInfoRes("Refundable Advance", 0.0, 0.0,
                    0.0, 0.0, "NA", null);
        } else {
//            if (PaymentStatus.PENDING.name().equals(advanceInvoice.getPaymentStatus())){
//                customerAdvanceInfoRes = new CustomerAdvanceInfoRes("Refundable Advance", 0.0, 0.0,
//                        0.0, 0.0, advanceInvoice.getInvoiceNumber(), null);
//            }

            double totalAmount = 0;
            double paidAmount = 0;
            double availableBalanceAmount = 0;
            if (advanceInvoice.getTotalAmount() != null){
                totalAmount = advanceInvoice.getTotalAmount();
            }
            if (advanceInvoice.getPaidAmount() != null){
                paidAmount = advanceInvoice.getPaidAmount();
            }
            if (advanceInvoice.getBalanceAmount() != null){
                availableBalanceAmount = advanceInvoice.getBalanceAmount();
            }

            double appliedAmount = paidAmount - availableBalanceAmount;

            List<InvoiceRedemption> invoiceRedemptions = invoiceRedemptionService
                    .getInvoiceRedemptionBySourceInvoiceId(advanceInvoice.getInvoiceId());

            List<RedemptionInfoRes> redeemedToRes = buildRedeemedToInfoRes(invoiceRedemptions);

            customerAdvanceInfoRes = new CustomerAdvanceInfoRes("Refundable Advance", totalAmount, paidAmount,
                    availableBalanceAmount, appliedAmount, advanceInvoice.getInvoiceNumber(), redeemedToRes);
        }

        return customerAdvanceInfoRes;
    }

    private CustomerBookingInfoRes buildBookingInfoRes(InvoicesV1 bookingInvoice) {

        CustomerBookingInfoRes customerBookingInfoRes = null;

        if (bookingInvoice == null){
            customerBookingInfoRes = new CustomerBookingInfoRes("Refundable Bookings", 0.0, 0.0,
                    0.0, 0.0, "NA", null);
        } else {

//            if (PaymentStatus.PENDING.name().equals(bookingInvoice.getPaymentStatus())){
//                customerBookingInfoRes = new CustomerBookingInfoRes("Refundable Bookings", 0.0, 0.0,
//                        0.0, 0.0, bookingInvoice.getInvoiceNumber(), null);
//            }

            double totalAmount = 0;
            double paidAmount = 0;
            double availableBalanceAmount = 0;
            if (bookingInvoice.getTotalAmount() != null){
                totalAmount = bookingInvoice.getTotalAmount();
            }
            if (bookingInvoice.getPaidAmount() != null){
                paidAmount = bookingInvoice.getPaidAmount();
            }
            if (bookingInvoice.getBalanceAmount() != null){
                availableBalanceAmount = bookingInvoice.getBalanceAmount();
            }

            double appliedAmount = paidAmount - availableBalanceAmount;

            List<InvoiceRedemption> invoiceRedemptions = invoiceRedemptionService
                    .getInvoiceRedemptionBySourceInvoiceId(bookingInvoice.getInvoiceId());

            List<RedemptionInfoRes> redeemedToRes = buildRedeemedToInfoRes(invoiceRedemptions);

            customerBookingInfoRes = new CustomerBookingInfoRes("Refundable Bookings", totalAmount, paidAmount,
                    availableBalanceAmount, appliedAmount, bookingInvoice.getInvoiceNumber(), redeemedToRes);
        }

        return customerBookingInfoRes;
    }

    private List<RedemptionInfoRes> buildRedeemedToInfoRes(List<InvoiceRedemption> invoiceRedemptions) {

        Set<String> targetInvoiceIds = invoiceRedemptions.stream()
                .map(InvoiceRedemption::getTargetInvoiceId)
                .collect(Collectors.toSet());

        List<InvoicesV1> targetInvoices = invoiceV1Service
                .getInvoicesByIds(targetInvoiceIds);

        Map<String, InvoicesV1> targetInvoiceMap = targetInvoices.stream()
                .collect(Collectors.toMap(InvoicesV1::getInvoiceId, Function.identity()));

        return invoiceRedemptions.stream()
                .map(redemption -> {
                    InvoicesV1 targetInvoice = targetInvoiceMap.getOrDefault(redemption.getTargetInvoiceId(), null);

                    String invoiceNumber = null;
                    String invoiceDate = null;
                    String invoiceType = null;
                    String defaultInvoiceType = null;
                    Double invoiceAmount = 0.0;

                    if (targetInvoice != null){
                        invoiceNumber = targetInvoice.getInvoiceNumber();
                        invoiceDate = Utils.dateToString(targetInvoice.getInvoiceStartDate());
                        invoiceAmount = targetInvoice.getTotalAmount();
                        if (InvoiceType.RENT.name().equals(targetInvoice.getInvoiceType())) {
                            invoiceType = "Rent";
                        }
                        else if (InvoiceType.REASSIGN_RENT.name().equals(targetInvoice.getInvoiceType())) {
                            invoiceType = "Rent";
                        }
                        else if (InvoiceType.ADVANCE.name().equals(targetInvoice.getInvoiceType())) {
                            invoiceType = "Advance";
                        }
                        defaultInvoiceType = targetInvoice.getInvoiceType();
                    }

                    return new RedemptionInfoRes(redemption.getTargetInvoiceId(), invoiceNumber, invoiceAmount,
                            invoiceDate, invoiceType, defaultInvoiceType, Utils.dateToString(redemption.getRedeemedAt()),
                            redemption.getRedemptionAmount());
                }).toList();
    }

    private List<PendingEbRes> buildPendingEbResponseByEbHistory(List<CustomersEbHistory> pendingCustomersEbHistories,
                                                                 List<CustomersBedHistory> customersBedHistories,
                                                                 Map<Integer, Floors> floorsMap,
                                                                 Map<Integer, Rooms> roomsMap,
                                                                 Map<Integer, Beds> bedsMap, Date leavingDate) {

        if (pendingCustomersEbHistories == null || pendingCustomersEbHistories.isEmpty()) {
            return Collections.emptyList();
        }

        return pendingCustomersEbHistories.stream()
                .map(ebHistory -> {

                    String floorName = null;
                    String roomName = null;
                    String bedName = null;

                    Rooms room = roomsMap.getOrDefault(ebHistory.getRoomId(), null);

                    Integer floorId = ebHistory.getFloorId();
                    Floors floor = floorsMap.getOrDefault(floorId, null);
                    if (room != null) {
                        floorId = room.getFloorId();
                        floor = floorsMap.getOrDefault(floorId, null);
                    }

                    floorName = floor != null ? floor.getFloorName() : null;
                    roomName = room != null ? room.getRoomName() : null;

                    Integer bedId = ebHistory.getBedId();
                    Beds bed = bedsMap.getOrDefault(bedId, null);
                    if (bed != null) {
                        bedName = bed.getBedName();
                    }

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

                    return new PendingEbRes(
                            floorId,
                            floorName,
                            ebHistory.getRoomId(),
                            roomName,
                            bedId,
                            bedName,
                            Utils.roundOfDoubleTo2Digits(ebHistory.getUnits()),
                            Utils.roundOfDoubleTo2Digits(ebHistory.getAmount()),
                            Utils.dateToString(startDate),
                            Utils.dateToString(endDate),
                            startDate, endDate
                    );
                })
                .toList();
    }

    private List<PendingEbRes> buildPendingEbResponse(List<ElectricityReadings> allPendingEbReadings,
                                                      Map<Integer, List<CustomersBedHistory>> roomHistoryMap,
                                                      Map<Integer, Floors> floorsMap,
                                                      Map<Integer, Rooms> roomsMap,
                                                      Map<Integer, Beds> bedsMap,
                                                      String customerId,
                                                      Date leavingDate,
                                                      ElectricityConfig ebConfig) {

        List<PendingEbRes> result = new ArrayList<>();

        double unitPrice = ebConfig != null && ebConfig.getCharge() != null
                ? ebConfig.getCharge()
                : 0;

        for (ElectricityReadings reading : allPendingEbReadings) {

            Rooms room = roomsMap.get(reading.getRoomId());

            Integer floorId = reading.getFloorId();
            Floors floor = floorsMap.get(floorId);

            if (room != null) {
                floorId = room.getFloorId();
                floor = floorsMap.get(floorId);
            }

            String floorName = floor != null ? floor.getFloorName() : null;
            String roomName = room != null ? room.getRoomName() : null;

            List<CustomersBedHistory> histories = roomHistoryMap
                    .getOrDefault(reading.getRoomId(), Collections.emptyList())
                    .stream()
                    .filter(history ->
                            Utils.compareWithTwoDates(history.getStartDate(), reading.getBillEndDate()) <= 0 &&
                                    (history.getEndDate() == null ||
                                            Utils.compareWithTwoDates(history.getEndDate(), reading.getBillStartDate()) >= 0))
                    .toList();

            if (histories.isEmpty()) {
                continue;
            }

            // ---------- PASS 1 : Calculate total person days ----------

            long totalPersonDays = 0;

            for (CustomersBedHistory history : histories) {

                Date historyEnd = history.getEndDate() == null
                        ? leavingDate
                        : history.getEndDate();

                Date overlapStart = Utils.compareWithTwoDates(
                        history.getStartDate(), reading.getBillStartDate()) > 0
                        ? history.getStartDate()
                        : reading.getBillStartDate();

                Date overlapEnd = Utils.compareWithTwoDates(
                        historyEnd, reading.getBillEndDate()) < 0
                        ? historyEnd
                        : reading.getBillEndDate();

                if (Utils.compareWithTwoDates(overlapStart, overlapEnd) > 0) {
                    continue;
                }

                totalPersonDays += Utils.findNumberOfDays(overlapStart, overlapEnd);
            }

            if (totalPersonDays == 0) {
                continue;
            }

            double unitsPerPersonDay = reading.getConsumption() / totalPersonDays;

            // ---------- PASS 2 : Create response only for this customer ----------

            for (CustomersBedHistory history : histories) {

                if (!customerId.equals(history.getCustomerId())) {
                    continue;
                }

                Date historyEnd = history.getEndDate() == null
                        ? leavingDate
                        : history.getEndDate();

                Date overlapStart = Utils.compareWithTwoDates(
                        history.getStartDate(), reading.getBillStartDate()) > 0
                        ? history.getStartDate()
                        : reading.getBillStartDate();

                Date overlapEnd = Utils.compareWithTwoDates(
                        historyEnd, reading.getBillEndDate()) < 0
                        ? historyEnd
                        : reading.getBillEndDate();

                if (Utils.compareWithTwoDates(overlapStart, overlapEnd) > 0) {
                    continue;
                }

                long stayDays = Utils.findNumberOfDays(overlapStart, overlapEnd);

                double units = unitsPerPersonDay * stayDays;
                double amount = units * unitPrice;

                Beds bed = bedsMap.get(history.getBedId());

                result.add(new PendingEbRes(
                        floorId,
                        floorName,
                        reading.getRoomId(),
                        roomName,
                        history.getBedId(),
                        bed != null ? bed.getBedName() : null,
                        Utils.roundOfDoubleTo2Digits(units),
                        Utils.roundOfDoubleTo2Digits(amount),
                        Utils.dateToString(overlapStart),
                        Utils.dateToString(overlapEnd),
                        overlapStart, overlapEnd
                ));
            }
        }

        return result;
    }

    private List<MissedEbRoomsRes> buildMissedEbRoomResponse(List<CustomersBedHistory> customersBedHistories,
                                                             List<ElectricityReadings> latestReadingOfRooms,
                                                             Map<Integer, Floors> floorsMap,
                                                             Map<Integer, Rooms> roomsMap,
                                                             Map<Integer, Beds> bedsMap,
                                                             Date leavingDate) {

        List<MissedEbRoomsRes> allMissedEbRoomsRes = new ArrayList<>();

        Map<Integer, ElectricityReadings> latestReadingMap = latestReadingOfRooms.stream()
                .collect(Collectors.toMap(
                        ElectricityReadings::getRoomId,
                        Function.identity(),
                        (a, b) -> a
                ));

        // Latest bed history for each room
        Map<Integer, CustomersBedHistory> latestBedHistoryMap = customersBedHistories.stream()
                .collect(Collectors.toMap(
                        CustomersBedHistory::getRoomId,
                        Function.identity(),
                        (h1, h2) -> {
                            Date end1 = h1.getEndDate() != null ? h1.getEndDate() : leavingDate;
                            Date end2 = h2.getEndDate() != null ? h2.getEndDate() : leavingDate;
                            return end1.after(end2) ? h1 : h2;
                        }
                ));

        for (CustomersBedHistory latestBedHistory : latestBedHistoryMap.values()) {

            ElectricityReadings latestReading = latestReadingMap.get(latestBedHistory.getRoomId());

            Date endDate = latestBedHistory.getEndDate() != null
                    ? latestBedHistory.getEndDate()
                    : leavingDate;

            if (latestReading != null &&
                    Utils.compareWithTwoDates(endDate, latestReading.getBillEndDate()) <= 0) {
                continue;
            }

            Rooms room = roomsMap.get(latestBedHistory.getRoomId());
            Beds bed = bedsMap.get(latestBedHistory.getBedId());

            Integer floorId = latestBedHistory.getFloorId();
            Floors floor = floorsMap.get(floorId);
            if (room != null) {
                floorId = room.getFloorId();
                floor = floorsMap.get(floorId);
            }

            String fromDate = Utils.dateToString(latestBedHistory.getStartDate());
            String toDate = Utils.dateToString(endDate);

            String lastEntryDate = null;
            Double lastReadingValue = 0.0;

            if (latestReading != null) {
                lastEntryDate = Utils.dateToString(latestReading.getEntryDate());
                lastReadingValue = latestReading.getCurrentReading();

                if (Utils.compareWithTwoDates(
                        latestReading.getEntryDate(),
                        latestBedHistory.getStartDate()) >= 0) {

                    fromDate = Utils.dateToString(
                            Utils.addDaysToDate(latestReading.getEntryDate(), 1));
                }
            }

            allMissedEbRoomsRes.add(new MissedEbRoomsRes(
                    latestBedHistory.getFloorId(),
                    floor != null ? floor.getFloorName() : null,
                    latestBedHistory.getRoomId(),
                    room != null ? room.getRoomName() : null,
                    latestBedHistory.getBedId(),
                    bed != null ? bed.getBedName() : null,
                    fromDate,
                    toDate,
                    lastReadingValue,
                    lastEntryDate,
                    latestReading != null &&
                            Utils.compareWithTwoDates(latestReading.getEntryDate(), latestBedHistory.getStartDate()) >= 0
                            ? Utils.addDaysToDate(latestReading.getEntryDate(), 1)
                            : latestBedHistory.getStartDate(),
                    endDate
            ));
        }

        return allMissedEbRoomsRes;
    }
}
