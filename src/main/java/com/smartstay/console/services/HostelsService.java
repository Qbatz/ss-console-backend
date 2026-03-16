package com.smartstay.console.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.console.Mapper.customers.CustomerResMapper;
import com.smartstay.console.Mapper.hostels.HostelDetailsMapper;
import com.smartstay.console.Mapper.hostels.HostelRecurringMapper;
import com.smartstay.console.Mapper.hostels.HostelsListMapper;
import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.Mapper.users.UsersResponseMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.dto.hostel.HostelResetSnapshot;
import com.smartstay.console.dto.hostelPlans.HostelPlanProjection;
import com.smartstay.console.ennum.*;
import com.smartstay.console.events.RecurringEvents;
import com.smartstay.console.payloads.hostel.HostelIdPayload;
import com.smartstay.console.payloads.hostel.HostelRecDatePayload;
import com.smartstay.console.repositories.HostelV1Repositories;
import com.smartstay.console.responses.customers.CustomerResponse;
import com.smartstay.console.responses.hostels.*;
import com.smartstay.console.responses.users.UserActivitiesResponse;
import com.smartstay.console.responses.users.UsersResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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

        HostelResponse hostelDetails = new HostelDetailsMapper(
                ownerInfo, noOfFloors, noOfRooms, noOfBeds, noOfActiveTenants, noOfBookedTenants,
                noOfCheckedInTenants, noOfNoticeTenants, noOfVacatedTenants, noOfTerminatedTenants,
                sharingTypeList, amenities, customerResponses, subscriptions, mastersRes, staffsRes,
                activities, userLookup
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

        double expenseAmount = listItemsExpense
                .stream()
                .mapToDouble(BankTransactionsV1::getAmount)
                .sum();

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
            double transactionAmount = 0.0;
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
            transactionV1Service.deleteALl(listTransactions);
        }
        if (listBeds != null && !listBeds.isEmpty()) {
            bedsService.makeAllBedAvailabe(listBeds);
        }
        if (customersList != null && !listBeds.isEmpty()) {
            customersService.deleteAll(customersList);
        }
        if (listItemsOtherThanExpense != null && !listItemsOtherThanExpense.isEmpty()) {
            bankTransactionService.deleteItemsOtherThanExpense(listItemsOtherThanExpense);
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

    public ResponseEntity<?> getAllHostelsNew(int page, int size, String hostelName) {
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


        Pageable pagebleRequest = PageRequest.of(page-1, size);
        Page<HostelV1> pageableHostelV1 = hostelRepository.findAllHostelsNew(hostelName, pagebleRequest);
        pageableHostelV1.getTotalElements();

        List<HostelV1> listHostels = pageableHostelV1.stream().toList();

        List<String> parentId = listHostels
                .stream()
                .map(HostelV1::getParentId)
                .toList();
        List<String> hostelIds = listHostels
                .stream()
                .map(HostelV1::getHostelId)
                .toList();
        List<String> parentIds = listHostels
                .stream()
                .map(HostelV1::getParentId)
                .toList();

        List<Users> createdUsers = usersService.getOwners(parentId);
        List<OwnerInfo> ownerInfos = createdUsers
                .stream()
                .map(i -> new UserOnerInfoMapper().apply(i))
                .toList();
        List<UserActivities> listActivities = userActivitiesService.findLatestActivities(hostelIds);
        List<LoginHistory> loginHistories = loginHistoryService.getLoginHistoriesByHostelIds(parentIds);

        List<HostelList> hostelsList = listHostels
                .stream()
                .map(i -> new HostelsListMapper(ownerInfos, listActivities, loginHistories).apply(i))
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

    public ResponseEntity<?> getHostelRecurring(int page, int size, String hostelName, String filterBy) {

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

        List<Map<String, String>> filterOptions = Arrays.stream(RecurringFilterOptions.values())
                .map(field -> Map.of(
                        "key", field.name(),
                        "label", field.getLabel()
                )).toList();

        Date today = new Date();

        Set<Integer> daySet = new HashSet<>();

        switch (filterOption) {

            case YESTERDAY -> daySet.add(Utils.getYesterdayDayOfMonth(today));

            case TWO_DAYS_AGO -> daySet.add(Utils.getTwoDaysAgoDayOfMonth(today));

            case TOMORROW -> daySet.add(Utils.getTomorrowDayOfMonth(today));

            case THIS_WEEK -> daySet.addAll(Utils.getThisWeekDays(today));

            case LAST_WEEK -> daySet.addAll(Utils.getLastWeekDays(today));

            case TILL_TODAY -> daySet.addAll(Utils.getDaysTillToday(today));

            case UP_COMING -> daySet.addAll(Utils.getUpcomingDays(today));

            default -> daySet.add(Utils.getDayOfMonth(today));
        }

        Page<BillingRules> paginatedBillingRules = billingRulesService
                .getPaginatedBillingRulesByDays(daySet,
                        hostelName == null || hostelName.isBlank() ? null : hostelName,
                        pageable);

        List<BillingRules> billingRulesList = paginatedBillingRules.getContent();

        List<HotelType> hotelTypes = hotelTypeService.getAllHotelTypes();
        Map<Integer, HotelType> hotelTypeMap = hotelTypes.stream()
                .collect(Collectors.toMap(HotelType::getId, hotelType -> hotelType));

        Set<String> hostelIds = billingRulesList.stream()
                .map(billingRules -> billingRules.getHostel().getHostelId())
                .collect(Collectors.toSet());

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

        List<HostelRecurringResponse> responseList = billingRulesList.stream()
                .map(billingRules -> new HostelRecurringMapper(
                        hotelTypeMap.get(billingRules.getHostel().getHostelType()),
                        recurringTrackerMap.get(billingRules.getHostel().getHostelId()),
                        agentMap
                        ).apply(billingRules)
                ).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("hostelList", responseList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", paginatedBillingRules.getTotalElements());
        response.put("totalPages", paginatedBillingRules.getTotalPages());
        response.put("filterOptions",  filterOptions);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> generateRecurring(String hostelId, HostelRecDatePayload hostelRecDatePayload) {

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

        Date today = new Date();
        int day = Utils.getDayOfMonth(today);
        if (hostelRecDatePayload.inputDay() != null){
            day = hostelRecDatePayload.inputDay();
        }

        BillingRules billingRules = billingRulesService.getCurrentMonthTemplate(hostelId);
        if (billingRules == null){
            return new ResponseEntity<>(Utils.NO_BILLING_RULE_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!billingRules.getBillingStartDate().equals(day)) {
            return new ResponseEntity<>(Utils.DAY_NOT_MATCH, HttpStatus.BAD_REQUEST);
        }
        if (!billingRules.getTypeOfBilling().equals("FIXED_DATE")){
            return new ResponseEntity<>(Utils.IS_NOT_FIXED_DATE, HttpStatus.BAD_REQUEST);
        }

        int billingDay = billingRules.getBillingStartDate();

        if (day < billingDay) {
            return new ResponseEntity<>(Utils.BILLING_DAY_NOT_REACHED, HttpStatus.BAD_REQUEST);
        }

        if (recurringTrackerService.checkRecurringTrackerExists(hostelId, billingRules.getBillingStartDate(),
                Utils.getCurrentMonth(today), Utils.getCurrentYear(today))){
            return new ResponseEntity<>(Utils.RECURRING_ALREADY_CREATED, HttpStatus.BAD_REQUEST);
        }

        try {
            applicationEventPublisher.publishEvent(new RecurringEvents(this, hostelId, billingDay));
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public List<HostelPlanProjection> getHostelPlanProjectionData(Set<String> parentIds) {
        return hostelRepository.findHostelPlanProjectionData(parentIds);
    }
}
