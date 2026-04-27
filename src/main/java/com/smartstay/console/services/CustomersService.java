package com.smartstay.console.services;

import com.smartstay.console.Mapper.customers.CustomerSumMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.customers.CustomerResetSnapshot;
import com.smartstay.console.dto.customers.CustomersCredentialsSnapshot;
import com.smartstay.console.dto.customers.CustomersSnapshot;
import com.smartstay.console.ennum.*;
import com.smartstay.console.payloads.customers.CustomerResetPayload;
import com.smartstay.console.repositories.CustomersRepository;
import com.smartstay.console.responses.customers.CustomerSummaryResponse;
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
import java.util.stream.Collectors;

import static com.smartstay.console.utils.AgentActivityUtil.cloneList;

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
    private CustomerWalletService customerWalletService;
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

        List<InvoicesV1> invoicesList = invoiceV1Service.findAllByHostelIdAndCustomerId(hostelId, customerId);
        List<BookingsV1> listBookings = bookingsService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<CustomersConfig> listConfigs = customersConfigService.findByHostelIdAndCustomerId(hostelId, customerId);
        CustomerCredentials customerCredentials = null;
        boolean isDuplicate = customersRepository.existsByXuidAndCustomerIdNot(customer.getXuid(), customerId);
        if (!isDuplicate) {
            customerCredentials = customersCredentialService.findByXuid(customer.getXuid());
        }
        List<CustomerDocuments> listCustomerDocuments = customerDocumentService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<CustomerAdditionalContacts> listCustomerAdditionalContacts = customerAdditionalContactsService
                .findByHostelIdAndCustomerId(hostelId, customerId);
        List<AmenityRequest> listAmenityRequests = amenityRequestService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<ComplaintsV1> complaints = complaintService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<CreditDebitNotes> listCreditDebits = creditDebitNotesService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<CustomersAmenity> listCustomersAmenity = customersAmenityService.findByCustomerId(customerId);
        List<CustomersBedHistory> listCustomerBedHistory = customerBedHistoryService.findByHostelIdAndCustomerId(hostelId, customerId);
        List<CustomersEbHistory> listCustomerEbHistory = customerEbHistoryService.findByCustomerId(customerId);
        List<CustomerWalletHistory> listCustomersWallet = customerWalletService.findByCustomerId(customerId);
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
                cloneList(invoicesList, InvoicesV1.class),
                cloneList(listBookings, BookingsV1.class),
                cloneList(listTransactions, TransactionV1.class),
                cloneList(listCustomersWallet, CustomerWalletHistory.class),
                cloneList(listCreditDebits, CreditDebitNotes.class),
                cloneList(complaints, ComplaintsV1.class),
                cloneList(listCustomerDocuments, CustomerDocuments.class),
                oldCredentials,
                cloneList(listCustomerAdditionalContacts, CustomerAdditionalContacts.class),
                cloneList(listCustomerBedHistory, CustomersBedHistory.class),
                cloneList(listCustomerEbHistory, CustomersEbHistory.class),
                cloneList(listCustomersAmenity, CustomersAmenity.class),
                cloneList(listAmenityRequests, AmenityRequest.class),
                cloneList(listConfigs, CustomersConfig.class),
                cloneList(listBankTransactions, BankTransactionsV1.class),
                cloneList(bankingList, BankingV1.class),
                cloneList(listBeds, Beds.class)
        );

        if (invoicesList != null && !invoicesList.isEmpty()) {
            invoiceV1Service.deleteAllInvoices(invoicesList);
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
            customerWalletService.deleteAll(listCustomersWallet);
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
            bedsService.makeAllBedAvailabe(listBeds);
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
}
