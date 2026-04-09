package com.smartstay.console.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.console.Mapper.customers.CustomerResMapper;
import com.smartstay.console.Mapper.hostels.*;
import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.Mapper.users.UsersResponseMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.dto.hostel.HostelResetSnapshot;
import com.smartstay.console.dto.hostel.InvoiceCountPerTracker;
import com.smartstay.console.dto.hostelPlans.HostelPlanProjection;
import com.smartstay.console.ennum.*;
import com.smartstay.console.events.PostpaidRecurringEvents;
import com.smartstay.console.events.RecurringEvents;
import com.smartstay.console.payloads.hostel.HostelIdPayload;
import com.smartstay.console.repositories.HostelV1Repositories;
import com.smartstay.console.responses.customers.CustomerResponse;
import com.smartstay.console.responses.hostels.*;
import com.smartstay.console.responses.users.UserActivitiesResponse;
import com.smartstay.console.responses.users.UsersResponse;
import com.smartstay.console.utils.Utils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.smartstay.console.utils.AgentActivityUtil.cloneList;

@Service
public class HostelsService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private HostelPlanService hostelPlansService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private UserActivitiesService userActivitiesService;
    @Autowired
    private HostelV1Repositories hostelRepository;
    @Autowired
    private FloorsService floorsService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private AmenitiesService amenitiesService;
    @Autowired
    private InvoiceV1Service invoiceV1Service;
    @Autowired
    private CustomerConfigService customersConfigService;
    @Autowired
    private CustomerDocumentService customerDocumentService;
    @Autowired
    private CustomersCredentialService customersCredentialService;
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
    private ElectricityReadingsService electricityReadingsService;
    @Autowired
    private HostelReadingService hostelReadingService;
    @Autowired
    private TransactionV1Service transactionV1Service;
    @Autowired
    private BankTransactionService bankTransactionService;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private BillingRulesService billingRulesService;
    @Autowired
    private HotelTypeService hotelTypeService;
    @Autowired
    private RecurringTrackerService recurringTrackerService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private LoginHistoryService loginHistoryService;
    @Autowired
    private PlansService plansService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private HostelPlanService hostelPlanService;

    public ResponseEntity<?> getHostelByHostelId(String hostelId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Hostels.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelRepository.findByHostelId(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        Users owner = usersService.getOwner(hostel.getParentId());
        OwnerInfo ownerInfo = new UserOnerInfoMapper().apply(owner);

        List<Users> masters = usersService.getMasters(hostel);
        Map<Users, Address> mastersAddressMap = masters.stream()
                .collect(Collectors.toMap(master -> master, Users::getAddress));
        List<UsersResponse> mastersRes = masters.stream()
                .map(users -> new UsersResponseMapper(
                        mastersAddressMap.get(users)
                ).apply(users)).toList();

        List<Users> staffs = usersService.getStaffs(hostel);
//        Map<Users, Address> staffsAddressMap = staffs.stream()
//                .collect(Collectors.toMap(staff -> staff, Users::getAddress));
        List<UsersResponse> staffsRes = staffs.stream()
                .map(users -> new UsersResponseMapper(
//                        staffsAddressMap.get(users)
                        null
                ).apply(users)).toList();

        List<Rooms> rooms = roomsService.getRoomsByHostelId(hostelId);
        List<Beds> beds = bedsService.getBedsByHostelId(hostelId);
        List<BookingsV1> bookings = bookingsService.getBookingsByHostelId(hostelId);

        Map<Integer, List<Rooms>> roomsBySharing = rooms.stream()
                .collect(Collectors.groupingBy(room ->
                        Optional.ofNullable(room.getSharingType()).orElse(0)
                ));

        Map<Integer, List<Beds>> bedsByRoom = beds.stream()
                .collect(Collectors.groupingBy(Beds::getRoomId));

        Set<String> activeStatuses = Set.of(
                BookingsStatus.CHECKIN.name(),
                BookingsStatus.BOOKED.name(),
                BookingsStatus.NOTICE.name()
        );

        Set<Integer> occupiedBedIds = bookings.stream()
                .filter(b -> activeStatuses.contains(b.getCurrentStatus()))
                .map(BookingsV1::getBedId)
                .collect(Collectors.toSet());

        List<SharingTypeResponse> sharingTypeList = new ArrayList<>();

        for (Map.Entry<Integer, List<Rooms>> entry : roomsBySharing.entrySet()) {

            Integer sharingType = entry.getKey();
            List<Rooms> sharingRooms = entry.getValue();

            int noOfRooms = sharingRooms.size();

            int noOfBeds = 0;
            int noOfOccupiedBeds = 0;
            int noOfAvailableRooms = 0;

            for (Rooms room : sharingRooms) {

                List<Beds> roomBeds = bedsByRoom.getOrDefault(room.getRoomId(), Collections.emptyList());

                int totalBedsInRoom = roomBeds.size();

                long occupiedBedsInRoom = roomBeds.stream()
                        .filter(b -> occupiedBedIds.contains(b.getBedId()))
                        .count();

                noOfBeds += totalBedsInRoom;
                noOfOccupiedBeds += (int) occupiedBedsInRoom;

                if (occupiedBedsInRoom < totalBedsInRoom) {
                    noOfAvailableRooms++;
                }
            }

            SharingTypeResponse sharingTypeResponse = new SharingTypeResponse(
                    sharingType,
                    sharingType + "-Sharing",
                    noOfRooms,
                    noOfBeds,
                    noOfOccupiedBeds,
                    noOfAvailableRooms
            );

            sharingTypeList.add(sharingTypeResponse);
        }

        sharingTypeList.sort(Comparator.comparing(SharingTypeResponse::sharingType));

        int noOfFloors = floorsService.getCountByHostelId(hostelId);
        int noOfRooms = rooms.size();
        int noOfBeds = beds.size();
        int noOfBookedTenants = 0;
        int noOfCheckedInTenants = 0;
        int noOfNoticeTenants = 0;
        int noOfVacatedTenants = 0;
        int noOfTerminatedTenants = 0;

        for (BookingsV1 booking : bookings){
            if (booking.getCurrentStatus().equalsIgnoreCase(BookingsStatus.BOOKED.name())){
                noOfBookedTenants++;
            } else if (booking.getCurrentStatus().equalsIgnoreCase(BookingsStatus.CHECKIN.name())) {
                noOfCheckedInTenants++;
            } else if (booking.getCurrentStatus().equalsIgnoreCase(BookingsStatus.NOTICE.name())) {
                noOfNoticeTenants++;
            } else if (booking.getCurrentStatus().equalsIgnoreCase(BookingsStatus.VACATED.name())) {
                noOfVacatedTenants++;
            } else if (booking.getCurrentStatus().equalsIgnoreCase(BookingsStatus.TERMINATED.name())) {
                noOfTerminatedTenants++;
            }
        }

        List<CustomerResponse> customerResponses = new ArrayList<>();

        if (agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Tenants.getId(), Utils.PERMISSION_READ)) {

            Set<String> customerIds = bookings.stream()
                    .map(BookingsV1::getCustomerId)
                    .collect(Collectors.toSet());

            List<Customers> customers = customersService.getCustomersByIds(customerIds);

            customerResponses = customers.stream()
                    .map(customer -> new CustomerResMapper().apply(customer))
                    .toList();
        }

        int noOfActiveTenants = noOfBookedTenants + noOfCheckedInTenants;

        List<Subscription> subscriptions = new ArrayList<>();

        if (agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Subscriptions.getId(), Utils.PERMISSION_READ)) {
            subscriptions = subscriptionService.getSubscriptionsByHostelId(hostelId);
        }

        List<UserActivities> activities = new ArrayList<>();
        if (agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Hostel_Activities.getId(), Utils.PERMISSION_READ)) {
            activities = userActivitiesService.getLimitedActivitiesByHostelId(hostelId, 50);
        }

        Map<String, Users> userLookup = new HashMap<>();
        userLookup.put(owner.getUserId(), owner);
        masters.forEach(master -> userLookup.put(master.getUserId(), master));
        staffs.forEach(staff -> userLookup.put(staff.getUserId(), staff));

        List<AmenitiesV1> amenities = amenitiesService.getAmenitiesByHostelId(hostelId);

        Plans trialPlan = plansService.findTrialPlan();
        Plans trialDaysPlan = plansService.findLatestTrialPlan();

        HostelResponse hostelDetails = new HostelDetailsMapper(
                ownerInfo, noOfFloors, noOfRooms, noOfBeds, noOfActiveTenants, noOfBookedTenants,
                noOfCheckedInTenants, noOfNoticeTenants, noOfVacatedTenants, noOfTerminatedTenants,
                sharingTypeList, amenities, customerResponses, subscriptions, mastersRes, staffsRes,
                activities, userLookup, trialPlan, trialDaysPlan
        ).apply(hostel);

        return new ResponseEntity<>(hostelDetails, HttpStatus.OK);
    }

    public ResponseEntity<?> resetHostelTenant(String hostelId, HostelIdPayload hostelIdPayload) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Hostel_Reset.getId(), Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        HostelV1 hostelV1 = hostelRepository.findByHostelId(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        if (!hostelV1.getHostelId().equals(hostelIdPayload.hostelId())){
            return new ResponseEntity<>(Utils.HOSTEL_ID_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        List<Customers> customersList = customersService.findCustomersByHostelId(hostelId);
        List<String> customerIds = customersList
                .stream()
                .map(Customers::getCustomerId)
                .toList();

        List<InvoicesV1> invoicesList = invoiceV1Service.findByListOfCustomers(hostelId, customerIds);
        List<BookingsV1> listBookings = bookingsService.findByHostelIdAndCustomerIds(hostelId, customerIds);
        List<CustomersConfig> listConfigs = customersConfigService.findByHostelIdAndCustomerIds(hostelId, customerIds);
        List<CustomerDocuments> listCustomerDocuments = customerDocumentService.findByHostelIdAndCustomerIds(hostelId, customerIds);
        List<CustomerCredentials> listCustomerCredentials = customersCredentialService.findByHostelIdAndCustomerIds(hostelId, customerIds);
        List<AmenityRequest> listAmenityRequests = amenityRequestService.findByHostelIdAndCustomerIds(hostelId, customerIds);
        List<ComplaintsV1> complaints = complaintService.findByHostelIdAndCustomerIdIn(hostelId, customerIds);
        List<CreditDebitNotes> listCreditDebits = creditDebitNotesService.findByHostelIdAndCustomerIds(hostelId, customerIds);
        List<CustomersAmenity> listCustomersAmenity = customersAmenityService.findByHostelIdAndCustomerIdIn(customerIds);
        List<CustomersBedHistory> listCustomerBedHistory = customerBedHistoryService.findByCustomerIds(hostelId, customerIds);
        List<CustomersEbHistory> listCustomerEbHistory = customerEbHistoryService.findByCustomerIdAndHostelId(hostelId, customerIds);
        List<CustomerWalletHistory> listCustomersWallet = customerWalletService.findByHostelIdAndCustomerIds(hostelId, customerIds);
        List<ElectricityReadings> listElectricityReadings = electricityReadingsService.findByHostelIdAndCustomerIdIn(hostelId, customerIds);
        List<HostelReadings> listHostelReadings = hostelReadingService.findByHostelId(hostelId);
        List<TransactionV1> listTransactions = transactionV1Service.findByHostelIdAndCustomerIds(hostelId, customerIds);
        List<Beds> listBeds = bedsService.findOccupiedBeds(hostelId);
        List<BankTransactionsV1> listBankTransactions = bankTransactionService.getAllTransactions(hostelId);
        List<BankingV1> bankingList = bankingService.findByHostelId(hostelId);

        List<BankTransactionsV1> listItemsExpense = listBankTransactions
                .stream()
                .filter(i -> i.getSource().equalsIgnoreCase(BankSource.EXPENSE.name()))
                .toList();

        List<BankTransactionsV1> listItemsOtherThanExpense = listBankTransactions
                .stream()
                .filter(i -> !i.getSource().equalsIgnoreCase(BankSource.EXPENSE.name()))
                .toList();

        HashMap<String, Double> bankBalances = new HashMap<>();

        HostelV1 oldHostel = new ObjectMapper().convertValue(hostelV1, HostelV1.class);

        HostelResetSnapshot snapshot = new HostelResetSnapshot(
                oldHostel,
                cloneList(customersList, Customers.class),
                cloneList(invoicesList, InvoicesV1.class),
                cloneList(listBookings, BookingsV1.class),
                cloneList(listTransactions, TransactionV1.class),
                cloneList(listCustomersWallet, CustomerWalletHistory.class),
                cloneList(listCreditDebits, CreditDebitNotes.class),
                cloneList(complaints, ComplaintsV1.class),
                cloneList(listCustomerDocuments, CustomerDocuments.class),
                cloneList(listCustomerBedHistory, CustomersBedHistory.class),
                cloneList(listCustomerEbHistory, CustomersEbHistory.class),
                cloneList(listCustomersAmenity, CustomersAmenity.class),
                cloneList(listAmenityRequests, AmenityRequest.class),
                cloneList(listConfigs, CustomersConfig.class),
                cloneList(listCustomerCredentials, CustomerCredentials.class),
                cloneList(listElectricityReadings, ElectricityReadings.class),
                cloneList(listHostelReadings, HostelReadings.class),
                cloneList(listBeds, Beds.class),
                cloneList(listBankTransactions, BankTransactionsV1.class),
                cloneList(bankingList, BankingV1.class)
        );

        if (invoicesList != null && !invoicesList.isEmpty()) {
            invoiceV1Service.deleteAllInvoices(invoicesList);
        }
        if (listBookings != null && !listBookings.isEmpty()) {
            bookingsService.deleteBookings(listBookings);
        }
        if (listConfigs != null && !listConfigs.isEmpty()) {
            customersConfigService.deleteAll(listConfigs);
        }
        if (listCustomerDocuments != null && !listCustomerDocuments.isEmpty()) {
            customerDocumentService.deleteDocuments(listCustomerDocuments);
        }
        if (listCustomerCredentials != null && !listCustomerCredentials.isEmpty()) {
            customersCredentialService.deleteCredentials(listCustomerCredentials);
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
        if (listElectricityReadings != null && !listElectricityReadings.isEmpty()) {
            electricityReadingsService.deleteAll(listElectricityReadings);
        }
        if (listHostelReadings != null && !listHostelReadings.isEmpty()) {
            hostelReadingService.deleteAll(listHostelReadings);
        }
        if (listTransactions != null && !listTransactions.isEmpty()) {
            transactionV1Service.deleteALl(listTransactions);
        }
        if (listBeds != null && !listBeds.isEmpty()) {
            bedsService.makeAllBedAvailabe(listBeds);
        }
        if (customersList != null && !customersList.isEmpty()) {
            customersService.deleteAll(customersList);
        }
        if (listItemsOtherThanExpense != null && !listItemsOtherThanExpense.isEmpty()) {
            bankTransactionService.deleteItemsOtherThanExpense(listItemsOtherThanExpense);
        }

        if (!listBankTransactions.isEmpty()) {
            listBankTransactions.forEach(item -> {
                double currentBalance = bankBalances.getOrDefault(item.getBankId(), 0.0);

                if (BankTransactionType.CREDIT.name().equalsIgnoreCase(item.getType())) {
                    currentBalance += item.getAmount();
                } else if (BankTransactionType.DEBIT.name().equalsIgnoreCase(item.getType())) {
                    currentBalance -= item.getAmount();
                }

                bankBalances.put(item.getBankId(), currentBalance);
            });
        }

        if (bankingList != null && !bankingList.isEmpty()) {
            List<BankingV1> newBalanceAmounts = bankingList
                    .stream()
                    .map(i -> {
                        if (bankBalances != null && bankBalances.get(i.getBankId()) != null) {
                            double amount = bankBalances.get(i.getBankId());
                            i.setBalance(i.getBalance() - amount);
                        }

                        return i;
                    })
                    .toList();
            bankingService.updateBankAccount(newBalanceAmounts);
        }

        if (!listItemsExpense.isEmpty()) {
            bankTransactionService.deleteExpenseItems(listItemsExpense);
            expenseService.deleteExpensesByHostelId(hostelId);
        }

        agentActivitiesService.createAgentActivity(agent, ActivityType.DELETE, Source.HOSTEL,
                hostelId, snapshot, null);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public List<HostelV1> getHostelsByParentId(String parentId) {
        return hostelRepository.findAllByParentId(parentId);
    }

    public ResponseEntity<?> getHostelActivities(String hostelId, int page, int size, String name) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Hostel_Activities.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelRepository.findByHostelId(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        Page<UserActivities> pagedActivities;
        List<UserActivities> userActivities;
        List<Users> users;
        Map<String, Users> usersMap;

        if (name != null && !name.isBlank()){
            users = usersService.getUsersByName(name);

            Set<String> userIds = users.stream()
                    .map(Users::getUserId)
                    .collect(Collectors.toSet());

            if (userIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("content", List.of());
                response.put("currentPage", page + 1);
                response.put("pageSize", size);
                response.put("totalItems", 0);
                response.put("totalPages", 0);

                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            pagedActivities = userActivitiesService
                    .getFilteredPaginatedActivitiesByHostelId(hostelId, userIds, pageable);

            userActivities = pagedActivities.getContent();
        } else {
            pagedActivities = userActivitiesService
                    .getPaginatedActivitiesByHostelId(hostelId, pageable);

            userActivities = pagedActivities.getContent();

            Set<String> userIds = userActivities.stream()
                    .map(UserActivities::getUserId)
                    .collect(Collectors.toSet());

            users = usersService.getUsersByIds(userIds);
        }

        usersMap = users.stream()
                .collect(Collectors.toMap(Users::getUserId, user -> user));

        List<UserActivitiesResponse> responseList = userActivities.stream()
                .map(activity -> {
                    Users user = usersMap.get(activity.getUserId());
                    String userName = null;
                    if (user != null){
                        userName = Utils.getFullName(user.getFirstName(), user.getLastName());
                    }
                    return new UserActivitiesResponse(
                            activity.getActivityId(), activity.getDescription(), activity.getUserId(), userName,
                            Utils.dateToString(activity.getCreatedAt()), Utils.dateToTime(activity.getCreatedAt()),
                            activity.getSource(), activity.getActivityType());
                }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content", responseList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedActivities.getTotalElements());
        response.put("totalPages", pagedActivities.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> removeExpenses(String hostelId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.EXPENSES.getId(), Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        return expenseService.deleteExpenses(hostelId, agent);
    }

    public ResponseEntity<?> getAllHostelsNew(int page, int size, String hostelName, Date startDate, Date endDate) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Hostels.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        long totalHostels = hostelRepository.findHostelCount();

        long activeHostels = hostelPlansService.findActiveHostels();

        long inactiveHostels = totalHostels - activeHostels;

        if (hostelName == null || hostelName.isBlank()){
            hostelName = null;
        }

        if (endDate != null) {
            endDate = Utils.addDaysToDate(endDate, 1);
        }

        Pageable pageableRequest = PageRequest.of(page-1, size);
        Page<HostelV1> pageableHostelV1 = hostelRepository
                .findAllHostelsNew(hostelName, startDate, endDate, pageableRequest);

        List<HostelV1> listHostels = pageableHostelV1.stream().toList();

        List<String> hostelIds = new ArrayList<>();
        List<String> parentIds = new ArrayList<>();

        for (HostelV1 hostel : listHostels) {
            hostelIds.add(hostel.getHostelId());
            parentIds.add(hostel.getParentId());
        }

        List<Users> createdUsers = usersService.getOwners(parentIds);
        List<OwnerInfo> ownerInfos = createdUsers
                .stream()
                .map(i -> new UserOnerInfoMapper().apply(i))
                .toList();
        List<UserActivities> listActivities = userActivitiesService
                .findLatestActivities(hostelIds);
        List<LoginHistory> loginHistories = loginHistoryService
                .getLoginHistoriesByHostelIds(parentIds);

        Map<String, OwnerInfo> ownerMap = ownerInfos.stream()
                .collect(Collectors.toMap(OwnerInfo::parentId, Function.identity(),
                        (a, b) -> a));
        Map<String, UserActivities> activityMap = listActivities.stream()
                .collect(Collectors.toMap(UserActivities::getHostelId, Function.identity(),
                        (a, b) -> a));
        Map<String, LoginHistory> loginMap = loginHistories.stream()
                .collect(Collectors.toMap(LoginHistory::getParentId, Function.identity(),
                        (a, b) -> a));

        Plans trialPlan = plansService.findTrialPlan();
        Plans trialDaysPlan = plansService.findLatestTrialPlan();

        List<Subscription> subscriptions = subscriptionService
                .getSubscriptionsByHostelIds(new HashSet<>(hostelIds));
        Map<String, List<Subscription>> subscriptionHostelMap = subscriptions.stream()
                .collect(Collectors.groupingBy(Subscription::getHostelId));

        List<HostelList> hostelsList = listHostels
                .stream()
                .map(i -> new HostelsListMapper(
                        ownerMap.getOrDefault(i.getParentId(), null),
                        activityMap.getOrDefault(i.getHostelId(), null),
                        loginMap.getOrDefault(i.getParentId(), null),
                        trialPlan,
                        trialDaysPlan,
                        subscriptionHostelMap.getOrDefault(i.getHostelId(), null)
                ).apply(i))
                .toList();

        Hostels hostels = new Hostels(totalHostels,
                activeHostels,
                inactiveHostels,
                pageableHostelV1.getPageable().getPageNumber()+1,
                size,
                pageableHostelV1.getTotalPages(),
                hostelsList);

        return new ResponseEntity<>(hostels, HttpStatus.OK);
    }

    public List<HostelList> getHostelsDataForExport(String hostelName, Date startDate, Date endDate){

        if (hostelName == null || hostelName.isBlank()){
            hostelName = null;
        }

        if (endDate != null) {
            endDate = Utils.addDaysToDate(endDate, 1);
        }

        List<HostelV1> listHostels = hostelRepository
                .findAllHostelsByNameAndJoiningDate(hostelName, startDate, endDate);

        Set<String> hostelIds = new HashSet<>();
        Set<String> parentIds = new HashSet<>();

        for (HostelV1 hostel : listHostels) {
            hostelIds.add(hostel.getHostelId());
            parentIds.add(hostel.getParentId());
        }

        List<Users> createdUsers = usersService.getOwners(new ArrayList<>(parentIds));

        List<OwnerInfo> ownerInfos = createdUsers
                .stream()
                .map(i -> new UserOnerInfoMapper().apply(i))
                .toList();

        List<UserActivities> listActivities = userActivitiesService
                .findLatestActivities(new ArrayList<>(hostelIds));

        List<LoginHistory> loginHistories = loginHistoryService
                .getLoginHistoriesByHostelIds(new ArrayList<>(parentIds));

        Map<String, OwnerInfo> ownerMap = ownerInfos.stream()
                .collect(Collectors.toMap(OwnerInfo::parentId, Function.identity(),
                        (a, b) -> a));

        Map<String, UserActivities> activityMap = listActivities.stream()
                .collect(Collectors.toMap(UserActivities::getHostelId, Function.identity(),
                        (a, b) -> a));

        Map<String, LoginHistory> loginMap = loginHistories.stream()
                .collect(Collectors.toMap(LoginHistory::getParentId, Function.identity(),
                        (a, b) -> a));

        Plans trialPlan = plansService.findTrialPlan();
        Plans trialDaysPlan = plansService.findLatestTrialPlan();

        List<Subscription> subscriptions = subscriptionService
                .getSubscriptionsByHostelIds(new HashSet<>(hostelIds));
        Map<String, List<Subscription>> subscriptionHostelMap = subscriptions.stream()
                .collect(Collectors.groupingBy(Subscription::getHostelId));

        return listHostels
                .stream()
                .map(i -> new HostelsListMapper(
                        ownerMap.getOrDefault(i.getParentId(), null),
                        activityMap.getOrDefault(i.getHostelId(), null),
                        loginMap.getOrDefault(i.getParentId(), null),
                        trialPlan,
                        trialDaysPlan,
                        subscriptionHostelMap.getOrDefault(i.getHostelId(), null)
                ).apply(i))
                .toList();
    }

    public ResponseEntity<?> getHostelRecurring(int page, int size, String hostelName,
                                                String filterBy, String statusFilterBy,
                                                String billingModelFilterBy, int billingCycleStartDay) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Recurring.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);
        Pageable pageable = PageRequest.of(page, size);

        RecurringFilterOptions filterOption =  RecurringFilterOptions.from(filterBy);
        RecurringStatusFilterOptions statusFilterOption = RecurringStatusFilterOptions.from(statusFilterBy);
        BillingModelFilterOptions billingModelFilterOption = BillingModelFilterOptions.from(billingModelFilterBy);

        List<Map<String, String>> filterOptions = Arrays.stream(RecurringFilterOptions.values())
                .map(field -> Map.of(
                        "key", field.name(),
                        "label", field.getLabel()
                )).toList();
        List<Map<String, String>> statusFilterOptions = Arrays.stream(RecurringStatusFilterOptions.values())
                .map(field -> Map.of(
                        "key", field.name(),
                        "label", field.getLabel()
                )).toList();
        List<Map<String, String>> billingModelFilterOptions = Arrays.stream(BillingModelFilterOptions.values())
                .map(field -> Map.of(
                        "key", field.name(),
                        "label", field.getLabel()
                )).toList();

        if (billingCycleStartDay < 0 || billingCycleStartDay > 31) {
            return new ResponseEntity<>(Utils.INVALID_BILLING_CYCLE_START_DAY, HttpStatus.BAD_REQUEST);
        }

        boolean isBillingCycleFilter = billingCycleStartDay > 0;

        if (isBillingCycleFilter && !filterBy.equals(RecurringFilterOptions.TODAY.name())) {
            return new ResponseEntity<>(Utils.CANNOT_USE_BILLING_CYCLE_FILTER_WITH_DATE_FILTER, HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        Set<Integer> daySet = new HashSet<>();

        int effectiveBillingDay = billingCycleStartDay;

        if (isBillingCycleFilter) {
            effectiveBillingDay = Math.min(billingCycleStartDay, Utils.getLastDayOfMonth(today));
            daySet.add(effectiveBillingDay);
        } else {
            switch (filterOption) {
                case YESTERDAY -> daySet.add(Utils.getYesterdayDayOfMonth(today));
                case TWO_DAYS_AGO -> daySet.add(Utils.getTwoDaysAgoDayOfMonth(today));
                case TOMORROW -> daySet.add(Utils.getTomorrowDayOfMonth(today));
                case THIS_WEEK -> daySet.addAll(Utils.getThisWeekDays(today));
                case LAST_WEEK -> daySet.addAll(Utils.getLastWeekDays(today));
                case TILL_TODAY -> daySet.addAll(Utils.getDaysTillToday(today));
                case UP_COMING -> daySet.addAll(Utils.getUpcomingDays(today));
                case THIS_MONTH -> daySet.addAll(Utils.getAllDaysOfMonth(today));
                default -> daySet.add(Utils.getDayOfMonth(today));
            }
        }

        boolean isStatusFilterApplied = !statusFilterOption.name().equals(RecurringStatusFilterOptions.ALL.name());
        boolean isHostelNameFilterApplied = hostelName != null && !hostelName.isBlank();

        Set<String> statusFilteredHostelIds = null;
        if (isStatusFilterApplied) {
            List<HostelV1> allHostels = hostelRepository.findAllHostels();
            Set<String> allHostelIds = allHostels.stream()
                    .map(HostelV1::getHostelId)
                    .collect(Collectors.toSet());
            Map<String, HostelV1> allHostelsMap = allHostels.stream()
                    .collect(Collectors.toMap(HostelV1::getHostelId,
                            Function.identity(), (a, b) -> a));

            List<BillingRules> latestBillingRules = billingRulesService
                    .getLatestBillingRulesByHostelIds(allHostelIds);
            Map<String, BillingRules> latestBillingRulesMap = latestBillingRules.stream()
                    .collect(Collectors.toMap(br -> br.getHostel().getHostelId(),
                            br -> br, (a, b) -> a));

            List<RecurringTracker> latestRecurringTrackers = recurringTrackerService
                    .getLatestRecurringTrackersByHostelIds(allHostelIds);
            Map<String, RecurringTracker> latestRecurringTrackersMap = latestRecurringTrackers.stream()
                            .collect(Collectors.toMap(RecurringTracker::getHostelId,
                                    Function.identity(), (a, b) -> a));

            Set<String> eligibleHostelIds = new HashSet<>();
            Set<String> generatedHostelIds = new HashSet<>();

            int currentMonth = Utils.getCurrentMonth(today);
            int currentYear = Utils.getCurrentYear(today);

            YearMonth previousYearMonth = Utils.getPreviousYearMonth(today);
            int previousMonth = previousYearMonth.getMonthValue();
            int previousYear = previousYearMonth.getYear();

            for (String hostelId : allHostelIds) {

                HostelV1 hostel = allHostelsMap.get(hostelId);
                BillingRules billingRules = latestBillingRulesMap.get(hostelId);
                RecurringTracker tracker = latestRecurringTrackersMap.get(hostelId);

                if (billingRules == null || hostel == null) continue;

                boolean isGenerated = false;
                boolean shouldConsider = true;

                int billingDay = billingRules.getBillingStartDate();

                if (BillingModel.POSTPAID.name().equals(billingRules.getBillingModel())) {

                    Date previousCycleDate = Utils.getPreviousMonthDate(today);
                    Date cycleStartDate = Utils.getDateFromDay(
                            billingDay,
                            Utils.getCurrentMonth(previousCycleDate),
                            Utils.getCurrentYear(previousCycleDate)
                    );

                    Date hostelCreatedDate = Utils.getStartOfDay(hostel.getCreatedAt());
                    Date cycleStart = Utils.getStartOfDay(cycleStartDate);

                    if (hostelCreatedDate.after(cycleStart)) {
                        shouldConsider = false;
                    }
                }

                if (!shouldConsider) {
                    if (tracker != null){
                        if (BillingModel.POSTPAID.name().equals(billingRules.getBillingModel())) {
                            isGenerated =
                                    tracker.getCreationDay() == billingDay &&
                                            tracker.getCreationMonth() == previousMonth &&
                                            tracker.getCreationYear() == previousYear;
                        }
                        if (isGenerated) {
                            generatedHostelIds.add(hostelId);
                            eligibleHostelIds.add(hostelId);
                        }
                    }
                    continue;
                }

                eligibleHostelIds.add(hostelId);

                if (tracker != null) {

                    if (BillingModel.PREPAID.name().equals(billingRules.getBillingModel())) {

                        isGenerated =
                                tracker.getCreationDay() == billingDay &&
                                        tracker.getCreationMonth() == currentMonth &&
                                        tracker.getCreationYear() == currentYear;

                    } else if (BillingModel.POSTPAID.name().equals(billingRules.getBillingModel())) {

                        isGenerated =
                                tracker.getCreationDay() == billingDay &&
                                        tracker.getCreationMonth() == previousMonth &&
                                        tracker.getCreationYear() == previousYear;
                    }
                }

                if (isGenerated) {
                    generatedHostelIds.add(hostelId);
                }
            }

            Set<String> notGeneratedHostelIds = new HashSet<>(eligibleHostelIds);
            notGeneratedHostelIds.removeAll(generatedHostelIds);

            if (statusFilterOption == RecurringStatusFilterOptions.GENERATED) {
                statusFilteredHostelIds = generatedHostelIds;
            } else {
                statusFilteredHostelIds = notGeneratedHostelIds;
            }
        }

        hostelName = hostelName == null || hostelName.isBlank() ? null : hostelName;

        Set<String> filteredHostelIds = null;
        if (hostelName != null) {
            List<HostelV1> filteredHostels = hostelService.getHostelsByHostelName(hostelName);
            filteredHostelIds = filteredHostels.stream()
                    .map(HostelV1::getHostelId)
                    .collect(Collectors.toSet());
        }

        Set<String> finalHostelIds = new HashSet<>();

        if (statusFilteredHostelIds != null && filteredHostelIds != null) {
            finalHostelIds = new HashSet<>(statusFilteredHostelIds);
            finalHostelIds.retainAll(filteredHostelIds);
        } else if (statusFilteredHostelIds != null) {
            finalHostelIds = statusFilteredHostelIds;
        } else if (filteredHostelIds != null) {
            finalHostelIds = filteredHostelIds;
        }

        boolean isAnyFilterApplied = isStatusFilterApplied || isHostelNameFilterApplied;

        if (isAnyFilterApplied && finalHostelIds.isEmpty()) {

            Map<String, Object> response = new HashMap<>();
            response.put("hostelList", Collections.emptyList());
            response.put("currentPage", page + 1);
            response.put("pageSize", size);
            response.put("totalItems", 0);
            response.put("totalPages", 0);
            response.put("recurringPendingCount", 0);
            response.put("subscriptionExpiredCount", 0);
            response.put("filterOptions", filterOptions);
            response.put("statusFilterOptions", statusFilterOptions);
            response.put("billingModelFilterOptions", billingModelFilterOptions);
            response.put("billingCycleStartDay", billingCycleStartDay);
            response.put("effectiveBillingDay", effectiveBillingDay);
            response.put("appliedFilterType", isBillingCycleFilter ? "BILLING_CYCLE" : "DATE_FILTER");

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        if (finalHostelIds.isEmpty()) {
            finalHostelIds = null;
        }

        String billingType = BillingType.FIXED_DATE.name();

        List<BillingRules> latestBillingRulesList = billingRulesService
                .getLatestBillingRulesByDays(daySet, billingType);

        Set<String> latestHostelIds = latestBillingRulesList.stream()
                .map(b -> b.getHostel().getHostelId())
                .collect(Collectors.toSet());

        List<RecurringTracker> trackers =
                recurringTrackerService.getLatestRecurringTrackersByHostelIds(latestHostelIds);

        Map<String, RecurringTracker> trackerMap = trackers.stream()
                .collect(Collectors.toMap(
                        RecurringTracker::getHostelId,
                        Function.identity(),
                        (a, b) -> a
                ));

        List<HostelPlan> plans = hostelPlanService.getPlansByHostelIds(latestHostelIds);

        Map<String, HostelPlan> planMap = plans.stream()
                .collect(Collectors.toMap(
                        p -> p.getHostel().getHostelId(),
                        Function.identity()
                ));

        long subscriptionExpiredCount = latestBillingRulesList.stream()
                .filter(b -> {
                    HostelPlan hp = planMap.get(b.getHostel().getHostelId());

                    return hp == null
                            || hp.getCurrentPlanEndsAt() == null
                            || hp.getCurrentPlanEndsAt().before(today);
                }).count();

        int currentMonth = Utils.getCurrentMonth(today);
        int currentYear = Utils.getCurrentYear(today);

        YearMonth previousYearMonth = Utils.getPreviousYearMonth(today);
        int previousMonth = previousYearMonth.getMonthValue();
        int previousYear = previousYearMonth.getYear();

        long recurringPendingCount = latestBillingRulesList.stream()
                .filter(b -> {
                    RecurringTracker r = trackerMap.get(b.getHostel().getHostelId());

                    if (r == null) return true;

                    int billingDay = b.getBillingStartDate();

                    if (BillingModel.PREPAID.name().equals(b.getBillingModel())) {
                        return !(r.getCreationDay() == billingDay
                                && r.getCreationMonth() == currentMonth
                                && r.getCreationYear() == currentYear);
                    }

                    if (BillingModel.POSTPAID.name().equals(b.getBillingModel())) {
                        return !(r.getCreationDay() == billingDay
                                && r.getCreationMonth() == previousMonth
                                && r.getCreationYear() == previousYear);
                    }

                    return true;
                }).count();

        Page<BillingRules> paginatedBillingRules = billingRulesService
                .getPaginatedBillingRulesByDays(daySet, billingType, finalHostelIds, billingModelFilterOption.name(), pageable);

        List<BillingRules> billingRulesList = paginatedBillingRules.getContent();

        List<HotelType> hotelTypes = hotelTypeService.getAllHotelTypes();
        Map<Integer, HotelType> hotelTypeMap = hotelTypes.stream()
                .collect(Collectors.toMap(HotelType::getId, hotelType -> hotelType));

        Set<String> hostelIds = new HashSet<>();
        Set<String> parentIds = new HashSet<>();

        for (BillingRules billingRules : billingRulesList) {
            hostelIds.add(billingRules.getHostel().getHostelId());
            parentIds.add(billingRules.getHostel().getParentId());
        }

        List<Users> owners = usersService.getOwners(new ArrayList<>(parentIds));

        Map<String, Users> ownerMap = owners.stream()
                .collect(Collectors.toMap(Users::getParentId,
                        user -> user));

        List<RecurringTracker> recurringTrackers = hostelIds.isEmpty()
                ? Collections.emptyList()
                : recurringTrackerService.getLatestRecurringTrackersByHostelIds(hostelIds);

        Map<String, RecurringTracker> recurringTrackerMap = recurringTrackers.stream()
                .collect(Collectors.toMap(RecurringTracker::getHostelId,
                        recurringTracker -> recurringTracker));

        Set<String> createdByIds = recurringTrackers.stream()
                .map(RecurringTracker::getCreatedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Agent> agents = createdByIds.isEmpty()
                ? Collections.emptyList()
                : agentService.getAgentsByIds(createdByIds);

        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId,
                        agent1 -> agent1));

        List<BookingsV1> bookings = bookingsService.getActiveBookingsByHostelIds(hostelIds);

        Map<String, List<BookingsV1>> bookingHostelMap = bookings.stream()
                .collect(Collectors.groupingBy(BookingsV1::getHostelId));

        List<HostelRecurringResponse> responseList = billingRulesList.stream()
                .map(billingRules -> new HostelRecurringMapper(
                        ownerMap.get(billingRules.getHostel().getParentId()),
                        hotelTypeMap.get(billingRules.getHostel().getHostelType()),
                        recurringTrackerMap.get(billingRules.getHostel().getHostelId()),
                        agentMap,
                        bookingHostelMap.get(billingRules.getHostel().getHostelId())
                        ).apply(billingRules)
                ).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("hostelList", responseList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", paginatedBillingRules.getTotalElements());
        response.put("totalPages", paginatedBillingRules.getTotalPages());
        response.put("recurringPendingCount", recurringPendingCount);
        response.put("subscriptionExpiredCount", subscriptionExpiredCount);
        response.put("filterOptions",  filterOptions);
        response.put("statusFilterOptions", statusFilterOptions);
        response.put("billingModelFilterOptions", billingModelFilterOptions);
        response.put("billingCycleStartDay", billingCycleStartDay);
        response.put("effectiveBillingDay", effectiveBillingDay);
        response.put("appliedFilterType", isBillingCycleFilter ? "BILLING_CYCLE" : "DATE_FILTER");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> generateRecurring(List<HostelIdPayload> payloads) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Recurring.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Date today = new Date();

        for (HostelIdPayload payload : payloads) {

            String hostelId = payload.hostelId();
            if (hostelId == null || hostelId.isBlank()){
                return new ResponseEntity<>(Utils.HOSTEL_ID_REQUIRED, HttpStatus.BAD_REQUEST);
            }

            HostelV1 hostel = hostelRepository.findByHostelId(hostelId);
            if (hostel == null){
                return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
            }

            HostelPlan hostelPlan = hostel.getHostelPlan();
            boolean isSubscriptionActive = false;
            if (hostelPlan != null && hostelPlan.getCurrentPlanEndsAt() != null) {
                isSubscriptionActive = Utils.compareWithTwoDates(
                        hostelPlan.getCurrentPlanEndsAt(), new Date()) >= 0;
            }

            if (!isSubscriptionActive){
                return new ResponseEntity<>(Utils.SUBSCRIPTION_NOT_ACTIVE, HttpStatus.BAD_REQUEST);
            }

            BillingRules billingRules = billingRulesService.getCurrentMonthTemplate(hostelId);
            if (billingRules == null){
                return new ResponseEntity<>(Utils.NO_BILLING_RULE_FOUND, HttpStatus.BAD_REQUEST);
            }

            int billingDay = billingRules.getBillingStartDate();

            if (!BillingType.FIXED_DATE.name().equals(billingRules.getTypeOfBilling())){
                return new ResponseEntity<>(Utils.IS_NOT_FIXED_DATE, HttpStatus.BAD_REQUEST);
            }

            int day = Utils.getDayOfMonth(today);
            if (day < billingDay) {
                return new ResponseEntity<>(Utils.BILLING_DAY_NOT_REACHED, HttpStatus.BAD_REQUEST);
            }
            if (billingDay != day) {
                return new ResponseEntity<>(Utils.DAY_NOT_MATCH, HttpStatus.BAD_REQUEST);
            }

            boolean isPostPaid = BillingModel.POSTPAID.name().equals(billingRules.getBillingModel());

            if (isPostPaid) {

                Date previousCycleDate = Utils.getPreviousMonthDate(today);

                Date cycleStartDate = Utils.getDateFromDay(
                        billingRules.getBillingStartDate(),
                        Utils.getCurrentMonth(previousCycleDate),
                        Utils.getCurrentYear(previousCycleDate)
                );

                Date hostelCreatedDate = Utils.getStartOfDay(hostel.getCreatedAt());
                Date cycleStart = Utils.getStartOfDay(cycleStartDate);

                if (hostelCreatedDate.after(cycleStart)) {
                    return new ResponseEntity<>(Utils.INVALID_RECURRING_CYCLE_FOR_POSTPAID, HttpStatus.BAD_REQUEST);
                }
            }

            if (recurringTrackerService.checkRecurringTrackerExists(hostelId, billingDay,
                    today, isPostPaid)){
                return new ResponseEntity<>(Utils.RECURRING_ALREADY_CREATED, HttpStatus.BAD_REQUEST);
            }

            try {
                if (BillingModel.PREPAID.name().equals(billingRules.getBillingModel())){
                    applicationEventPublisher.publishEvent(new RecurringEvents(this, hostelId, billingDay));
                } else if (BillingModel.POSTPAID.name().equals(billingRules.getBillingModel())) {
                    applicationEventPublisher.publishEvent(new PostpaidRecurringEvents(this, hostelId, billingDay));
                }
            } catch (Exception e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public List<HostelPlanProjection> getHostelPlanProjectionData(Set<String> parentIds) {
        return hostelRepository.findHostelPlanProjectionData(parentIds);
    }

    public long getHostelCount(){
        return hostelRepository.findHostelCount();
    }

    public ResponseEntity<?> getRecurringHistory(String hostelId, int page, int size) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Recurring.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);
        Pageable pageable = PageRequest.of(page, size);

        HostelV1 hostel = hostelRepository.findByHostelId(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        Page<RecurringTracker> paginatedRecurringTrackers = recurringTrackerService
                .getPaginatedRecurringTrackersByHostelId(hostelId, pageable);

        List<RecurringTracker> recurringTrackers = paginatedRecurringTrackers.getContent();

        Set<String> createdByIds = new HashSet<>();
        Set<Long> trackerIds = new HashSet<>();

        for (RecurringTracker recurringTracker : recurringTrackers) {
            if (recurringTracker.getCreatedBy() != null){
                createdByIds.add(recurringTracker.getCreatedBy());
            }
            trackerIds.add(recurringTracker.getTrackerId());
        }

        List<Agent> agents = createdByIds.isEmpty()
                ? Collections.emptyList()
                : agentService.getAgentsByIds(createdByIds);

        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId,
                        agent1 -> agent1));

        List<InvoiceCountPerTracker> invoiceCountPerTrackers = recurringTrackerService
                .getGeneratedInvoiceCountPerTracker(trackerIds);

        Map<Long, Long> invoiceCountPerTrackerMap = invoiceCountPerTrackers.stream()
                .collect(Collectors.toMap(
                        InvoiceCountPerTracker::trackerId,
                        InvoiceCountPerTracker::invoiceCount
                ));

        List<RecurringHistoryRes> recurringHistory = recurringTrackers.stream()
                .map(recurringTracker -> new RecurringHistoryMapper(
                        hostel, agentMap, invoiceCountPerTrackerMap.getOrDefault(recurringTracker.getTrackerId(), 0L)
                ).apply(recurringTracker))
                .toList();

        List<HotelType> hotelTypes = hotelTypeService.getAllHotelTypes();
        Map<Integer, HotelType> hotelTypeMap = hotelTypes.stream()
                .collect(Collectors.toMap(HotelType::getId, hotelType -> hotelType));

        Users owner = usersService.getOwner(hostel.getParentId());

        List<BookingsV1> bookings = bookingsService.findCheckedInCustomers(hostelId);

        RecurringTracker latestRecurringTracker = recurringTrackerService
                .getLatestRecurringTrackerByHostelId(hostelId);

        BillingRules billingRules = billingRulesService.getCurrentMonthTemplate(hostelId);

        RecurringTrackerRes recurringTrackerRes = new RecurringTrackerResMapper(
                hotelTypeMap.get(hostel.getHostelType()),
                owner,
                bookings,
                billingRules,
                latestRecurringTracker,
                page + 1,
                size,
                paginatedRecurringTrackers,
                recurringHistory
        ).apply(hostel);

        return new ResponseEntity<>(recurringTrackerRes, HttpStatus.OK);
    }

    public void exportHostels(String hostelName, Date startDate, Date endDate, HttpServletResponse response) throws IOException {

        if (!authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, Utils.UN_AUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, Utils.UN_AUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Hostels.getId(), Utils.PERMISSION_READ)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, Utils.ACCESS_RESTRICTED);
        }

        List<HostelList> hostels = getHostelsDataForExport(hostelName, startDate, endDate);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Hostels");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Hostel Name");
        header.createCell(1).setCellValue("Country Code");
        header.createCell(2).setCellValue("Mobile");
        header.createCell(3).setCellValue("City");
        header.createCell(4).setCellValue("State");
        header.createCell(5).setCellValue("Address");
        header.createCell(6).setCellValue("Owner Name");
        header.createCell(7).setCellValue("Owner Country Code");
        header.createCell(8).setCellValue("Owner Mobile");
        header.createCell(9).setCellValue("Hostel Plan Name");
        header.createCell(10).setCellValue("Hostel Plan Code");
        header.createCell(11).setCellValue("Hostel Plan Amount");
        header.createCell(12).setCellValue("Hostel Joined On");
        header.createCell(13).setCellValue("Plan Expired On");
        header.createCell(14).setCellValue("Plan Expiring At");
        header.createCell(15).setCellValue("Is Trial");
        header.createCell(16).setCellValue("Is Trial Extendable");
        header.createCell(17).setCellValue("Is Subscription Active");
        header.createCell(18).setCellValue("Subscription Active Days");
        header.createCell(19).setCellValue("Last Updated Date");
        header.createCell(20).setCellValue("Last Updated Time");
        header.createCell(21).setCellValue("Platform");

        int rowIdx = 1;
        for (HostelList h : hostels) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(h.hostelName() != null ? h.hostelName() : "");
            row.createCell(1).setCellValue(h.countryCode() != null ? h.countryCode() : "");
            row.createCell(2).setCellValue(h.mobile() != null ? h.mobile() : "");
            row.createCell(3).setCellValue(h.city() != null ? h.city() : "");
            row.createCell(4).setCellValue(h.state() != null ? h.state() : "");
            row.createCell(5).setCellValue(h.fullAddress() != null  ? h.fullAddress() : "");
            row.createCell(6).setCellValue(h.ownerInfo() != null ? h.ownerInfo().fullName() != null ? h.ownerInfo().fullName() : "" : "");
            row.createCell(7).setCellValue(h.ownerInfo() != null ? h.ownerInfo().countryCode() != null ? h.ownerInfo().countryCode() : "" : "");
            row.createCell(8).setCellValue(h.ownerInfo() != null ? h.ownerInfo().mobile() != null ? h.ownerInfo().mobile() : "" : "");
            row.createCell(9).setCellValue(h.hostelPlan() != null ? h.hostelPlan().currentPlan() != null ? h.hostelPlan().currentPlan() : "" : "");
            row.createCell(10).setCellValue(h.hostelPlan() != null ? h.hostelPlan().currentPlanCode() != null ? h.hostelPlan().currentPlanCode() : "" : "");
            row.createCell(11).setCellValue(h.hostelPlan() != null ? h.hostelPlan().currentPlanAmount() != null ? h.hostelPlan().currentPlanAmount() : 0D : 0D);
            row.createCell(12).setCellValue(h.joinedOn() != null ? h.joinedOn() : "");
            row.createCell(13).setCellValue(h.expiredOn() != null ? h.expiredOn() : "");
            row.createCell(14).setCellValue(h.expiringAt() !=  null ? h.expiringAt() : "");
            row.createCell(15).setCellValue(h.isTrial());
            row.createCell(16).setCellValue(h.trialExtendable());
            row.createCell(17).setCellValue(h.subscriptionIsActive());
            row.createCell(18).setCellValue(h.noOfdaysSubscriptionActive());
            row.createCell(19).setCellValue(h.lastUpdateDate() != null ? h.lastUpdateDate() : "");
            row.createCell(20).setCellValue(h.lastUpdateTime() != null ? h.lastUpdateTime() : "");
            row.createCell(21).setCellValue(h.platform() != null ? h.platform() : "");
        }

        Row headerRow = sheet.getRow(0);
        int totalColumns = headerRow.getLastCellNum();

        for (int col = 0; col < totalColumns; col++) {
            sheet.autoSizeColumn(col);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"hostels.xlsx\"");

        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
        workbook.close();
    }
}
