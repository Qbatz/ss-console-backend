package com.smartstay.console.services;

import com.smartstay.console.Mapper.customers.CustomerRecHistoryMapper;
import com.smartstay.console.Mapper.customers.CustomerRecTrackerResMapper;
import com.smartstay.console.Mapper.customers.CustomerRecurringMapper;
import com.smartstay.console.Mapper.customers.CustomerResMapper;
import com.smartstay.console.Mapper.hostels.*;
import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.Mapper.users.UsersResponseMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.dto.hostel.HostelResetSnapshot;
import com.smartstay.console.dto.hostel.HostelSnapshot;
import com.smartstay.console.dto.hostel.InvoiceCountPerTracker;
import com.smartstay.console.dto.hostelPlans.HostelPlanProjection;
import com.smartstay.console.ennum.*;
import com.smartstay.console.events.JoiningBasedPrepaidEvents;
import com.smartstay.console.events.PostpaidRecurringEvents;
import com.smartstay.console.events.RecurringEvents;
import com.smartstay.console.payloads.customers.CustomerIdPayload;
import com.smartstay.console.payloads.hostel.HostelIdPayload;
import com.smartstay.console.repositories.HostelV1Repositories;
import com.smartstay.console.responses.customers.CustomerRecHistoryRes;
import com.smartstay.console.responses.customers.CustomerRecTrackerRes;
import com.smartstay.console.responses.customers.CustomerRecurringResponse;
import com.smartstay.console.responses.customers.CustomerResponse;
import com.smartstay.console.responses.hostels.*;
import com.smartstay.console.responses.users.UserActivitiesResponse;
import com.smartstay.console.responses.users.UsersResponse;
import com.smartstay.console.utils.SnapshotUtility;
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
    private UserHostelService userHostelService;
    @Autowired
    private UserActivitiesService userActivitiesService;
    @Autowired
    private HostelPlanService hostelPlansService;
    @Autowired
    private SubscriptionService subscriptionService;
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
    @Autowired
    private CustomerRecurringTrackerService customerRecurringTrackerService;
    @Autowired
    private CustomerAdditionalContactsService customerAdditionalContactsService;

    public List<HostelV1> getHostelsByParentId(String parentId) {
        return hostelRepository.findAllByParentIdAndIsActiveTrueAndIsDeletedFalse(parentId);
    }

    public List<HostelPlanProjection> getHostelPlanProjectionData(Set<String> parentIds) {
        return hostelRepository.findHostelPlanProjectionData(parentIds);
    }

    public long getHostelCount(){
        return hostelRepository.findHostelCount();
    }

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

        HostelV1 hostel = hostelRepository.findByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
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

        List<Plans> trialPlans = plansService.findTrialPlans();
        List<Plans> expandableTrialPlans = plansService.findExpandableTrialPlans();

        Date today = new Date();
        Date prevMonthDate = Utils.getPreviousMonthDate(today);

        List<BillingRules> billingRulesList = hostel.getBillingRulesList();
        Map<Integer, BillingDates> billingDatesMap = new HashMap<>();

        for (BillingRules br : billingRulesList){

            BillingDates billingDates;

            if (BillingType.FIXED_DATE.name().equals(br.getTypeOfBilling())){
                if (BillingModel.PREPAID.name().equals(br.getBillingModel())){
                    billingDates = billingRulesService.computeBillingDates(br, today);
                    billingDatesMap.put(br.getId(), billingDates);
                } else if (BillingModel.POSTPAID.name().equals(br.getBillingModel())) {
                    billingDates = billingRulesService.computeBillingDates(br, prevMonthDate);
                    billingDatesMap.put(br.getId(), billingDates);
                }
            } else if (BillingType.JOINING_DATE_BASED.name().equals(br.getTypeOfBilling())) {
                billingDatesMap.put(br.getId(), null);
            }
        }

        List<RecurringTracker> recurringTrackers = recurringTrackerService
                .getRecurringTrackersByHostelId(hostelId);

        Set<String> createdByIds = new HashSet<>();
        Set<Long> trackerIds = new HashSet<>();

        for (RecurringTracker recurringTracker : recurringTrackers) {
            if (recurringTracker.getCreatedBy() != null){
                createdByIds.add(recurringTracker.getCreatedBy());
            }
            trackerIds.add(recurringTracker.getTrackerId());
        }

        List<CustomerRecurringTracker> customerRecurringTrackers = customerRecurringTrackerService
                .getRecurringTrackersByHostelId(hostelId);

        Set<String> customerIds = new HashSet<>();
        for (CustomerRecurringTracker recurringTracker : customerRecurringTrackers) {
            if (recurringTracker.getCreatedBy() != null){
                createdByIds.add(recurringTracker.getCreatedBy());
            }
            customerIds.add(recurringTracker.getCustomerId());
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

        List<Customers> customers = customersService.getCustomersByIds(customerIds);
        Map<String, Customers> customersMap = customers.stream()
                .collect(Collectors.toMap(Customers::getCustomerId,
                        customer -> customer, (a, b) -> a));

        List<CustomerRecHistoryRes> customerRecurringHistory = customerRecurringTrackers.stream()
                .map(recurringTracker -> new CustomerRecHistoryMapper(
                        customersMap.getOrDefault(recurringTracker.getCustomerId(), null),
                        hostel,
                        agentMap.getOrDefault(recurringTracker.getCreatedBy(), null)
                ).apply(recurringTracker)).toList();

        boolean recurringStatus = false;
        Date currentBillLastRecDate = null;
        BillingRules currentBillingRules = billingRulesService.getCurrentMonthTemplate(hostelId);

        if (currentBillingRules != null) {
            int startDay = currentBillingRules.getBillingStartDate();
            BillingDates billingDates = billingDatesMap
                    .getOrDefault(currentBillingRules.getId(), null);
            Date startDate = today;

            if (billingDates != null) {
                startDate = billingDates.currentBillStartDate();
            }

            if (BillingType.FIXED_DATE.name().equals(currentBillingRules.getTypeOfBilling())){
                RecurringTracker latestTracker = recurringTrackerService
                        .getLatestRecurringTrackerByHostelId(hostelId);
                if (latestTracker != null) {
                    recurringStatus = Utils.isSameBillingCycle(startDay, latestTracker, startDate);
                    currentBillLastRecDate = Utils.getDateFromDay(latestTracker.getCreationDay(),
                            latestTracker.getCreationMonth(), latestTracker.getCreationYear());
                }
            } else if (BillingType.JOINING_DATE_BASED.name().equals(currentBillingRules.getTypeOfBilling())) {
                CustomerRecurringTracker latestTracker = customerRecurringTrackerService
                        .getLatestTrackerByHostelId(hostelId);
                if (latestTracker != null) {
                    recurringStatus = Utils.isSameBillingCycle(startDay, latestTracker, startDate);
                    currentBillLastRecDate = Utils.getDateFromDay(latestTracker.getCreationDay(),
                            latestTracker.getCreationMonth(), latestTracker.getCreationYear());
                }
            }
        }

        HostelResponse hostelDetails = new HostelDetailsMapper(
                ownerInfo, noOfFloors, noOfRooms, noOfBeds, noOfActiveTenants, noOfBookedTenants,
                noOfCheckedInTenants, noOfNoticeTenants, noOfVacatedTenants, noOfTerminatedTenants,
                sharingTypeList, amenities, customerResponses, subscriptions, mastersRes, staffsRes,
                activities, userLookup, trialPlans, expandableTrialPlans, billingDatesMap, recurringHistory,
                customerRecurringHistory, recurringStatus, currentBillLastRecDate, currentBillingRules
        ).apply(hostel);

        return new ResponseEntity<>(hostelDetails, HttpStatus.OK);
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

        HostelV1 hostel = hostelRepository.findByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
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

    public void resetHostel(HostelV1 hostel, Agent loggedInAgent){

        String hostelId = hostel.getHostelId();

        List<Customers> customersList = customersService.findCustomersByHostelId(hostelId);
        List<String> customerIds = customersList
                .stream()
                .map(Customers::getCustomerId)
                .toList();

        List<InvoicesV1> invoicesList = invoiceV1Service.findByListOfCustomers(hostelId, customerIds);
        List<BookingsV1> listBookings = bookingsService.findByHostelIdAndCustomerIds(hostelId, customerIds);
        List<CustomersConfig> listConfigs = customersConfigService.findByHostelIdAndCustomerIds(hostelId, customerIds);
        List<CustomerDocuments> listCustomerDocuments = customerDocumentService.findByHostelIdAndCustomerIds(hostelId, customerIds);
        List<CustomerAdditionalContacts> listCustomerAdditionalContacts = customerAdditionalContactsService
                .findByHostelIdAndCustomerIds(hostelId, customerIds);
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

        HostelSnapshot oldHostel = SnapshotUtility.toSnapshot(hostel);

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
                cloneList(listCustomerAdditionalContacts, CustomerAdditionalContacts.class),
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
        if (listCustomerAdditionalContacts != null && !listCustomerAdditionalContacts.isEmpty()) {
            customerAdditionalContactsService.deleteAll(listCustomerAdditionalContacts);
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

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.DELETE, Source.HOSTEL,
                hostelId, snapshot, null);
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

        HostelV1 hostelV1 = hostelRepository.findByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        if (!hostelV1.getHostelId().equals(hostelIdPayload.hostelId())){
            return new ResponseEntity<>(Utils.HOSTEL_ID_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        try {
            resetHostel(hostelV1, agent);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> deleteHostel(String hostelId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Hostels.getId(), Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelRepository.findByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
        if (hostel == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        HostelSnapshot oldHostel = SnapshotUtility.toSnapshot(hostel);

        try {
            resetHostel(hostel, agent);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        List<UserHostel> userHostels = userHostelService.getUsersByHostelId(hostelId);

        if (!userHostels.isEmpty()) {

            Set<String> userIds = userHostels.stream()
                    .map(UserHostel::getUserId)
                    .collect(Collectors.toSet());

            List<Users> nonMasterUsers = usersService.getStaffs(hostel.getParentId(), userIds);

            if (!nonMasterUsers.isEmpty()) {

                Set<String> nonMasterUserIds = nonMasterUsers.stream()
                        .map(Users::getUserId)
                        .collect(Collectors.toSet());

                List<UserActivities> userActivities = userActivitiesService
                        .getUserActivitiesByUserIds(nonMasterUserIds);

                if (!userActivities.isEmpty()){
                    userActivitiesService.deleteAll(userActivities);
                }

                usersService.deleteAll(nonMasterUsers);
            }

            userHostelService.deleteAll(userHostels);
        }

        hostelRepository.delete(hostel);

        agentActivitiesService.createAgentActivity(agent, ActivityType.DELETE, Source.HOSTEL,
                hostelId, oldHostel, null);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
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

        List<Plans> trialPlans = plansService.findTrialPlans();
        List<Plans> expandableTrialPlans = plansService.findExpandableTrialPlans();

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
                        trialPlans,
                        expandableTrialPlans,
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

        List<Plans> trialPlans = plansService.findTrialPlans();
        List<Plans> expandableTrialPlans = plansService.findExpandableTrialPlans();

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
                        trialPlans,
                        expandableTrialPlans,
                        subscriptionHostelMap.getOrDefault(i.getHostelId(), null)
                ).apply(i))
                .toList();
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
        header.createCell(16).setCellValue("Can Add Trial");
        header.createCell(17).setCellValue("Can Add Expandable Trial");
        header.createCell(18).setCellValue("Is Subscription Active");
        header.createCell(19).setCellValue("Subscription Active Days");
        header.createCell(20).setCellValue("Last Updated Date");
        header.createCell(21).setCellValue("Last Updated Time");
        header.createCell(22).setCellValue("Platform");

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
            row.createCell(16).setCellValue(h.canAddTrial());
            row.createCell(17).setCellValue(h.canAddExpandableTrial());
            row.createCell(18).setCellValue(h.subscriptionIsActive());
            row.createCell(19).setCellValue(h.noOfdaysSubscriptionActive());
            row.createCell(20).setCellValue(h.lastUpdateDate() != null ? h.lastUpdateDate() : "");
            row.createCell(21).setCellValue(h.lastUpdateTime() != null ? h.lastUpdateTime() : "");
            row.createCell(22).setCellValue(h.platform() != null ? h.platform() : "");
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

        String billingType = BillingType.FIXED_DATE.name();

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

        List<BillingRules> latestBillingRulesList = billingRulesService
                .getLatestBillingRulesByDays(daySet, billingType);

        long totalProperties = latestBillingRulesList.size();

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

        Set<String> createdByIds = trackers.stream()
                .map(RecurringTracker::getCreatedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Agent> agents = createdByIds.isEmpty()
                ? Collections.emptyList()
                : agentService.getAgentsByIds(createdByIds);

        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId,
                        agent1 -> agent1));

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

        long recurringPendingCount = latestBillingRulesList.stream()
                .filter(b -> {
                    RecurringTracker r = trackerMap.get(b.getHostel().getHostelId());

                    if (r == null) return true;

                    int billingDay = b.getBillingStartDate();

                    String billingModel = b.getBillingModel();
                    boolean isPrePaid = BillingModel.PREPAID.name().equals(billingModel);
                    boolean isPostPaid = BillingModel.POSTPAID.name().equals(billingModel);

                    BillingDates billingDates = null;

                    if (isPrePaid){
                        billingDates = billingRulesService.computeBillingDates(b, today);
                    } else if (isPostPaid) {
                        Date previousMonthDate = Utils.getPreviousMonthDate(today);
                        billingDates = billingRulesService.computeBillingDates(b, previousMonthDate);
                    }

                    if (billingDates == null) {
                        return true;
                    }

                    Date cycleStartDate = billingDates.currentBillStartDate();

                    int cycleMonth = Utils.getCurrentMonth(cycleStartDate);
                    int cycleYear = Utils.getCurrentYear(cycleStartDate);

                    return !(r.getCreationDay() == billingDay
                            && r.getCreationMonth() == cycleMonth
                            && r.getCreationYear() == cycleYear);
                }).count();

        boolean isStatusFilterApplied = !statusFilterOption.name().equals(RecurringStatusFilterOptions.ALL.name());
        boolean isHostelNameFilterApplied = hostelName != null && !hostelName.isBlank();

        Set<String> statusFilteredHostelIds = null;
        if (isStatusFilterApplied) {

            Set<String> eligibleHostelIds = new HashSet<>();
            Set<String> generatedHostelIds = new HashSet<>();

            for (BillingRules billingRules : latestBillingRulesList) {

                String hostelId = billingRules.getHostel().getHostelId();
                HostelV1 hostel = billingRules.getHostel();
                RecurringTracker tracker = trackerMap.get(hostelId);

                int billingDay = billingRules.getBillingStartDate();

                String billingModel = billingRules.getBillingModel();
                boolean isPrePaid = BillingModel.PREPAID.name().equals(billingModel);
                boolean isPostPaid = BillingModel.POSTPAID.name().equals(billingModel);

                BillingDates billingDates = null;

                if (isPrePaid){
                    billingDates = billingRulesService.computeBillingDates(billingRules, today);
                } else if (isPostPaid) {
                    Date previousMonthDate = Utils.getPreviousMonthDate(today);
                    billingDates = billingRulesService.computeBillingDates(billingRules, previousMonthDate);
                }

                if (billingDates == null) {
                    continue;
                }

                Date cycleStartDate = billingDates.currentBillStartDate();

                int cycleMonth = Utils.getCurrentMonth(cycleStartDate);
                int cycleYear = Utils.getCurrentYear(cycleStartDate);

                boolean isGenerated = false;
                boolean shouldConsider = true;

                if (BillingModel.POSTPAID.name().equals(billingRules.getBillingModel())) {

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
                                            tracker.getCreationMonth() == cycleMonth &&
                                            tracker.getCreationYear() == cycleYear;
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
                    isGenerated =
                            tracker.getCreationDay() == billingDay &&
                                    tracker.getCreationMonth() == cycleMonth &&
                                    tracker.getCreationYear() == cycleYear;
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
            response.put("totalProperties", totalProperties);
            response.put("recurringPendingCount", recurringPendingCount);
            response.put("subscriptionExpiredCount", subscriptionExpiredCount);
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

        List<BookingsV1> bookings = bookingsService.getActiveBookingsByHostelIds(hostelIds);

        Map<String, List<BookingsV1>> bookingHostelMap = bookings.stream()
                .collect(Collectors.groupingBy(BookingsV1::getHostelId));

        List<HostelRecurringResponse> responseList = billingRulesList.stream()
                .map(billingRules -> {
                            String billingModel = billingRules.getBillingModel();
                            boolean isPrePaid = BillingModel.PREPAID.name().equals(billingModel);
                            boolean isPostPaid = BillingModel.POSTPAID.name().equals(billingModel);

                            BillingDates billingDates = null;

                            if (isPrePaid){
                                billingDates = billingRulesService.computeBillingDates(billingRules, today);
                            } else if (isPostPaid) {
                                Date previousMonthDate = Utils.getPreviousMonthDate(today);
                                billingDates = billingRulesService.computeBillingDates(billingRules, previousMonthDate);
                            }

                            return new HostelRecurringMapper(
                                    ownerMap.get(billingRules.getHostel().getParentId()),
                                    hotelTypeMap.get(billingRules.getHostel().getHostelType()),
                                    trackerMap.getOrDefault(billingRules.getHostel().getHostelId(), null),
                                    agentMap,
                                    bookingHostelMap.get(billingRules.getHostel().getHostelId()),
                                    billingDates
                            ).apply(billingRules);
                        }
                ).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("hostelList", responseList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", paginatedBillingRules.getTotalElements());
        response.put("totalPages", paginatedBillingRules.getTotalPages());
        response.put("totalProperties", totalProperties);
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

    public ResponseEntity<?> getMonthlyRecurringSummary(int month, int year) {

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

        if (month < 1 || month > 12) {
            return new ResponseEntity<>(Utils.INVALID_MONTH, HttpStatus.BAD_REQUEST);
        }

        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        List<MonthlyRecRes> responseList = new ArrayList<>();

        String billingType = BillingType.FIXED_DATE.name();

        Date baseDate = Utils.getDateFromDay(1, month, year);

        Set<Integer> monthDaySet = Utils.getAllDaysOfMonth(baseDate);

        List<BillingRules> billingRulesList =
                billingRulesService.getLatestBillingRulesByDays(monthDaySet, billingType);
        Map<Integer, List<BillingRules>> billingRulesDayMap = billingRulesList.stream()
                .collect(Collectors.groupingBy(BillingRules::getBillingStartDate));

        Set<String> hostelIds = billingRulesList.stream()
                .map(b -> b.getHostel().getHostelId())
                .collect(Collectors.toSet());

        List<RecurringTracker> trackers =
                recurringTrackerService.getLatestRecurringTrackersByHostelIds(hostelIds);
        Map<String, RecurringTracker> trackerMap = trackers.stream()
                .collect(Collectors.toMap(
                        RecurringTracker::getHostelId,
                        Function.identity(),
                        (a, b) -> a
                ));

        List<HostelPlan> hostelPlans = hostelPlanService.getPlansByHostelIds(hostelIds);
        Map<String, HostelPlan> hostelPlanMap = hostelPlans.stream()
                .collect(Collectors.toMap(hp -> hp.getHostel().getHostelId(),
                        Function.identity(), (a,b) -> a));

        for (int day = 1; day <= daysInMonth; day++) {

            List<BillingRules> dayBillingRulesList =
                    billingRulesDayMap.getOrDefault(day, Collections.emptyList());

            if (dayBillingRulesList.isEmpty()) {
                responseList.add(new MonthlyRecRes(day, month, year, 0, 0, 0));
                continue;
            }

            long totalProperties = dayBillingRulesList.size();

            Date dateFromDay = Utils.getDateFromDay(day, month, year);
            long subscriptionExpiredCount = dayBillingRulesList.stream()
                    .filter(b -> {
                        HostelPlan hp = hostelPlanMap.get(b.getHostel().getHostelId());

                        return hp == null
                                || hp.getCurrentPlanEndsAt() == null
                                || !hp.getCurrentPlanEndsAt().after(dateFromDay);
                    }).count();

            long recurringPendingCount = dayBillingRulesList.stream()
                    .filter(b -> {
                        RecurringTracker r = trackerMap.get(b.getHostel().getHostelId());

                        if (r == null) return true;

                        String billingModel = b.getBillingModel();
                        boolean isPrePaid = BillingModel.PREPAID.name().equals(billingModel);
                        boolean isPostPaid = BillingModel.POSTPAID.name().equals(billingModel);

                        BillingDates billingDates = null;

                        if (isPrePaid){
                            billingDates = billingRulesService.computeBillingDates(b, dateFromDay);
                        } else if (isPostPaid) {
                            Date previousMonthDate = Utils.getPreviousMonthDate(dateFromDay);
                            billingDates = billingRulesService.computeBillingDates(b, previousMonthDate);
                        }

                        if (billingDates == null) {
                            return true;
                        }

                        Date cycleStartDate = billingDates.currentBillStartDate();
                        int billingDay = b.getBillingStartDate();
                        int cycleMonth = Utils.getCurrentMonth(cycleStartDate);
                        int cycleYear = Utils.getCurrentYear(cycleStartDate);

                        return !(r.getCreationDay() == billingDay
                                && r.getCreationMonth() == cycleMonth
                                && r.getCreationYear() == cycleYear);
                    }).count();

            responseList.add(new MonthlyRecRes(
                    day, month, year,
                    totalProperties,
                    recurringPendingCount,
                    subscriptionExpiredCount
            ));
        }

        return new ResponseEntity<>(responseList, HttpStatus.OK);
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

            HostelV1 hostel = hostelRepository.findByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
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

            String billingModel = billingRules.getBillingModel();
            boolean isPrePaid = BillingModel.PREPAID.name().equals(billingModel);
            boolean isPostPaid = BillingModel.POSTPAID.name().equals(billingModel);

            BillingDates billingDates = null;

            if (isPrePaid){
                billingDates = billingRulesService.computeBillingDates(billingRules, today);
            } else if (isPostPaid) {
                Date previousMonthDate = Utils.getPreviousMonthDate(today);
                billingDates = billingRulesService.computeBillingDates(billingRules, previousMonthDate);
            }

            if (billingDates == null) {
                return new ResponseEntity<>(Utils.NO_BILLING_RULE_FOUND, HttpStatus.BAD_REQUEST);
            }

            Date currentBillStartDate = billingDates.currentBillStartDate();

            if (recurringTrackerService.checkRecurringTrackerExists(hostelId, billingDay, currentBillStartDate)){
                return new ResponseEntity<>(Utils.RECURRING_ALREADY_CREATED, HttpStatus.BAD_REQUEST);
            }

            try {
                if (isPrePaid){
                    applicationEventPublisher.publishEvent(new RecurringEvents(this,
                            hostelId, billingDay, currentBillStartDate, billingDates));
                } else if (isPostPaid) {
                    applicationEventPublisher.publishEvent(new PostpaidRecurringEvents(this,
                            hostelId, billingDay, billingDates));
                }
            } catch (Exception e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
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

        HostelV1 hostel = hostelRepository.findByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
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

        if (billingRules == null){
            return new ResponseEntity<>(Utils.NO_BILLING_RULE_FOUND, HttpStatus.BAD_REQUEST);
        }

        String billingModel = billingRules.getBillingModel();
        boolean isPrePaid = BillingModel.PREPAID.name().equals(billingModel);
        boolean isPostPaid = BillingModel.POSTPAID.name().equals(billingModel);

        BillingDates billingDates = null;
        Date today = new Date();

        if (isPrePaid){
            billingDates = billingRulesService.computeBillingDates(billingRules, today);
        } else if (isPostPaid) {
            Date previousMonthDate = Utils.getPreviousMonthDate(today);
            billingDates = billingRulesService.computeBillingDates(billingRules, previousMonthDate);
        }

        RecurringTrackerRes recurringTrackerRes = new RecurringTrackerResMapper(
                hotelTypeMap.get(hostel.getHostelType()),
                owner,
                bookings,
                billingRules,
                billingDates,
                latestRecurringTracker,
                page + 1,
                size,
                paginatedRecurringTrackers,
                recurringHistory
        ).apply(hostel);

        return new ResponseEntity<>(recurringTrackerRes, HttpStatus.OK);
    }

    public ResponseEntity<?> getTenantRecurring(int page, int size, String name,
                                                String filterBy, String statusFilterBy,
                                                String billingModelFilterBy, int billingCycleStartDay,
                                                boolean isHostelBased) {

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

        String billingType = BillingType.JOINING_DATE_BASED.name();

        long totalTenants = 0;
        long billingToday = 0;
        long billingTomorrow = 0;

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

        if (isBillingCycleFilter && !RecurringFilterOptions.TODAY.name().equals(filterBy)) {
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

        List<Customers> daySetCustomers = customersService
                .getCustomersByDays(daySet);

        Map<String, Customers> daySetCustomersMap = daySetCustomers.stream()
                .collect(Collectors.toMap(Customers::getCustomerId,
                        Function.identity(), (a, b) -> a));

        Set<String> daySetCustomerIds = daySetCustomers.stream()
                .map(Customers::getCustomerId)
                .collect(Collectors.toSet());
        Set<String> hostelIds = daySetCustomers.stream()
                .map(Customers::getHostelId)
                .collect(Collectors.toSet());

        List<HostelV1> hostels = hostelRepository.findAllByHostelIdInAndIsActiveTrueAndIsDeletedFalse(hostelIds);

        Map<String, HostelV1> hostelMap = hostels.stream()
                .collect(Collectors.toMap(HostelV1::getHostelId,
                        Function.identity(), (a, b) -> a));

        Set<String> parentIds = hostels.stream()
                .map(HostelV1::getParentId)
                .collect(Collectors.toSet());

        List<BillingRules> billingRulesList = billingRulesService
                .getLatestBillingRulesByHostelIdsAndBillingType(hostelIds, billingType);
        Map<String, BillingRules> billingRulesMap = billingRulesList.stream()
                .collect(Collectors.toMap(br -> br.getHostel().getHostelId(),
                        br -> br, (a, b) -> a));

        Set<String> billingTypeFilteredHostelIds = billingRulesList.stream()
                .map(br -> br.getHostel().getHostelId())
                .collect(Collectors.toSet());
        List<BookingsV1> activeCustomers = bookingsService
                .getActiveBookingsByHostelIds(billingTypeFilteredHostelIds);

        Map<String, List<BookingsV1>> activeCustomersMap = activeCustomers.stream()
                .collect(Collectors.groupingBy(BookingsV1::getHostelId));

        Set<String> activeCustomersIds = activeCustomers.stream()
                .map(BookingsV1::getCustomerId)
                .collect(Collectors.toSet());

        Set<String> activeDaySetCustomerIds = new HashSet<>(daySetCustomerIds);
        activeDaySetCustomerIds.retainAll(activeCustomersIds);

        totalTenants = activeDaySetCustomerIds.size();

        for (String customerId : activeDaySetCustomerIds) {
            Customers customer = daySetCustomersMap.getOrDefault(customerId, null);
            if (customer != null) {
                Date joinedDate = customer.getJoiningDate() != null ? customer.getJoiningDate() : customer.getExpJoiningDate();
                if (joinedDate != null) {
                    int day = Utils.getDayOfMonth(joinedDate);
                    if (day == Utils.getDayOfMonth(today)){
                        billingToday++;
                    } else if (day == Utils.getTomorrowDayOfMonth(today)) {
                        billingTomorrow++;
                    }
                }
            }
        }

        List<CustomerRecurringTracker> latestTrackers = customerRecurringTrackerService
                .getLatestTrackersByCustomerIds(activeDaySetCustomerIds);
        Map<String, CustomerRecurringTracker> latestTrackerMap = latestTrackers.stream()
                .collect(Collectors.toMap(CustomerRecurringTracker::getCustomerId,
                        crt -> crt, (a, b) -> a));

        Set<String> createdByIds = latestTrackers.stream()
                .map(CustomerRecurringTracker::getCreatedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Agent> agents = createdByIds.isEmpty()
                ? Collections.emptyList()
                : agentService.getAgentsByIds(createdByIds);

        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId,
                        agent1 -> agent1, (a, b) -> a));

        boolean isStatusFilterApplied = !statusFilterOption.name().equals(RecurringStatusFilterOptions.ALL.name());
        boolean isNameFilterApplied = name != null && !name.isBlank();

        Set<String> statusFilteredCustomerIds = null;
        if (isStatusFilterApplied) {

            Set<String> eligibleCustomerIds = new HashSet<>();
            Set<String> generatedCustomerIds = new HashSet<>();

            for (String customerId : activeDaySetCustomerIds) {

                Customers customer = daySetCustomersMap.get(customerId);
                if (customer == null) {continue;}

                HostelV1 hostel = hostelMap.get(customer.getHostelId());
                BillingRules billingRules = billingRulesMap.get(customer.getHostelId());
                CustomerRecurringTracker tracker = latestTrackerMap.get(customerId);

                if (billingRules == null || hostel == null) continue;

                String billingModel = billingRules.getBillingModel();
                boolean isPrePaid = BillingModel.PREPAID.name().equals(billingModel);
                boolean isPostPaid = BillingModel.POSTPAID.name().equals(billingModel);

                Date joinedDate = customer.getJoiningDate() != null ? customer.getJoiningDate() : customer.getExpJoiningDate();
                if (joinedDate == null) continue;

                int billingDay = Utils.getDayOfMonth(joinedDate);

                BillingDates billingDates = null;
                if (isPrePaid){
                    billingDates = billingRulesService.computeJoiningBasedBillingDates(billingRules, joinedDate, today);
                } else if (isPostPaid) {
                    Date previousMonthDate = Utils.getPreviousMonthDate(today);
                    billingDates = billingRulesService.computeJoiningBasedBillingDates(billingRules, joinedDate, previousMonthDate);
                }

                if (billingDates == null) continue;

                boolean isGenerated = false;
                boolean shouldConsider = true;

                Date cycleStartDate = billingDates.currentBillStartDate();

                if (BillingModel.POSTPAID.name().equals(billingRules.getBillingModel())) {

                    Date hostelCreatedDate = Utils.getStartOfDay(hostel.getCreatedAt());
                    Date cycleStart = Utils.getStartOfDay(cycleStartDate);

                    if (hostelCreatedDate.after(cycleStart)) {
                        shouldConsider = false;
                    }
                }

                int cycleMonth = Utils.getCurrentMonth(cycleStartDate);
                int cycleYear = Utils.getCurrentYear(cycleStartDate);

                if (!shouldConsider) {
                    if (tracker != null){
                        if (BillingModel.POSTPAID.name().equals(billingRules.getBillingModel())) {
                            isGenerated =
                                    tracker.getCreationDay() == billingDay &&
                                            tracker.getCreationMonth() == cycleMonth &&
                                            tracker.getCreationYear() == cycleYear;
                        }
                        if (isGenerated) {
                            generatedCustomerIds.add(customerId);
                            eligibleCustomerIds.add(customerId);
                        }
                    }
                    continue;
                }

                eligibleCustomerIds.add(customerId);

                if (tracker != null) {
                    isGenerated =
                            tracker.getCreationDay() == billingDay &&
                                    tracker.getCreationMonth() == cycleMonth &&
                                    tracker.getCreationYear() == cycleYear;
                }

                if (isGenerated) {
                    generatedCustomerIds.add(customerId);
                }
            }

            Set<String> notGeneratedCustomerIds = new HashSet<>(eligibleCustomerIds);
            notGeneratedCustomerIds.removeAll(generatedCustomerIds);

            if (statusFilterOption == RecurringStatusFilterOptions.GENERATED) {
                statusFilteredCustomerIds = generatedCustomerIds;
            } else {
                statusFilteredCustomerIds = notGeneratedCustomerIds;
            }
        }

        name = name == null || name.isBlank() ? null : name;

        Set<String> filteredCustomerIds = null;
        if (name != null) {
            List<Customers> filteredCustomers = customersService.getCustomersByName(name);
            filteredCustomerIds = filteredCustomers.stream()
                    .map(Customers::getCustomerId)
                    .collect(Collectors.toSet());
        }

        Set<String> finalCustomerIds = new HashSet<>();

        if (statusFilteredCustomerIds != null && filteredCustomerIds != null) {
            finalCustomerIds = new HashSet<>(statusFilteredCustomerIds);
            finalCustomerIds.retainAll(filteredCustomerIds);
        } else if (statusFilteredCustomerIds != null) {
            finalCustomerIds = statusFilteredCustomerIds;
        } else if (filteredCustomerIds != null) {
            finalCustomerIds = filteredCustomerIds;
        }

        boolean isAnyFilterApplied = isStatusFilterApplied || isNameFilterApplied;

        if (isAnyFilterApplied && finalCustomerIds.isEmpty()) {

            Map<String, Object> response = new HashMap<>();
            response.put("customerList", Collections.emptyList());
            response.put("hostelList", Collections.emptyList());
            response.put("currentPage", page + 1);
            response.put("pageSize", size);
            response.put("totalItems", 0);
            response.put("totalPages", 0);
            response.put("totalTenants", totalTenants);
            response.put("billingToday", billingToday);
            response.put("billingTomorrow",  billingTomorrow);
            response.put("filterOptions", filterOptions);
            response.put("statusFilterOptions", statusFilterOptions);
            response.put("billingModelFilterOptions", billingModelFilterOptions);
            response.put("billingCycleStartDay", billingCycleStartDay);
            response.put("effectiveBillingDay", effectiveBillingDay);
            response.put("appliedFilterType", isBillingCycleFilter ? "BILLING_CYCLE" : "DATE_FILTER");
            response.put("isHostelBased", isHostelBased);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        if (finalCustomerIds.isEmpty()) {
            finalCustomerIds = null;
        }

        Set<String> billingModelFilteredHostelIds = billingRulesList.stream()
                .map(br -> {
                    if (!billingModelFilterOption.name().equals(BillingModelFilterOptions.ALL.name())){
                        if (billingModelFilterOption.name().equals(br.getBillingModel())){
                            return br.getHostel() != null ? br.getHostel().getHostelId() : null;
                        } else {
                            return null;
                        }
                    }
                    return br.getHostel() != null ? br.getHostel().getHostelId() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> bookingCustomerIds = activeCustomers.stream()
                .filter(b -> billingModelFilteredHostelIds.contains(b.getHostelId()))
                .map(BookingsV1::getCustomerId)
                .collect(Collectors.toSet());

        Set<String> finalFilteredCustomerIds = new HashSet<>(daySetCustomerIds);
        finalFilteredCustomerIds.retainAll(bookingCustomerIds);

        if (finalCustomerIds != null) {
            finalFilteredCustomerIds.retainAll(finalCustomerIds);
        }

        if (finalFilteredCustomerIds.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("customerList", Collections.emptyList());
            response.put("hostelList", Collections.emptyList());
            response.put("currentPage", page + 1);
            response.put("pageSize", size);
            response.put("totalItems", 0);
            response.put("totalPages", 0);
            response.put("totalTenants", totalTenants);
            response.put("billingToday", billingToday);
            response.put("billingTomorrow",  billingTomorrow);
            response.put("filterOptions", filterOptions);
            response.put("statusFilterOptions", statusFilterOptions);
            response.put("billingModelFilterOptions", billingModelFilterOptions);
            response.put("billingCycleStartDay", billingCycleStartDay);
            response.put("effectiveBillingDay", effectiveBillingDay);
            response.put("appliedFilterType", isBillingCycleFilter ? "BILLING_CYCLE" : "DATE_FILTER");
            response.put("isHostelBased", isHostelBased);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Map<String, Object> response = new HashMap<>();
        long totalItems = 0;
        long totalPages = 0;

        List<Users> owners = usersService.getOwners(new ArrayList<>(parentIds));

        Map<String, Users> ownerMap = owners.stream()
                .collect(Collectors.toMap(Users::getParentId,
                        user -> user));

        List<HotelType> hotelTypes = hotelTypeService.getAllHotelTypes();
        Map<Integer, HotelType> hotelTypeMap = hotelTypes.stream()
                .collect(Collectors.toMap(HotelType::getId, hotelType -> hotelType));

        response.put("customerList", Collections.emptyList());
        response.put("hostelList", Collections.emptyList());
        if (isHostelBased){
            Map<String, Set<String>> hostelToCustomerIds = finalFilteredCustomerIds.stream()
                    .map(daySetCustomersMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(
                            Customers::getHostelId,
                            Collectors.mapping(Customers::getCustomerId, Collectors.toSet())
                    ));

           Set<String> hostelBasedHostelIds = hostelToCustomerIds.keySet();

            if (hostelBasedHostelIds.isEmpty()) {
                response.put("currentPage", page + 1);
                response.put("pageSize", size);
                response.put("totalItems", 0);
                response.put("totalPages", 0);
                response.put("totalTenants", totalTenants);
                response.put("billingToday", billingToday);
                response.put("billingTomorrow",  billingTomorrow);
                response.put("filterOptions", filterOptions);
                response.put("statusFilterOptions", statusFilterOptions);
                response.put("billingModelFilterOptions", billingModelFilterOptions);
                response.put("billingCycleStartDay", billingCycleStartDay);
                response.put("effectiveBillingDay", effectiveBillingDay);
                response.put("appliedFilterType", isBillingCycleFilter ? "BILLING_CYCLE" : "DATE_FILTER");
                response.put("isHostelBased", isHostelBased);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            Page<HostelV1> paginatedHostels = hostelRepository
                    .findAllByHostelIdIn(hostelBasedHostelIds, pageable);

            List<HostelV1> hostelBasedHostels = paginatedHostels.getContent();

            List<HostelTenRecResponse> responseList = hostelBasedHostels.stream()
                    .map(hostel -> {
                        Set<String> hostelBasedCusIds = hostelToCustomerIds
                                .getOrDefault(hostel.getHostelId(), Collections.emptySet());
                        List<Customers> hostelBasedCustomers = hostelBasedCusIds.stream()
                                .map(id -> daySetCustomersMap.getOrDefault(id, null))
                                .filter(Objects::nonNull)
                                .toList();

                        Users owner = ownerMap.getOrDefault(hostel.getParentId(), null);;
                        HotelType hotelType = hotelTypeMap
                                .getOrDefault(hostel.getHostelType(), null);
                        BillingRules hostelBillingRules = billingRulesMap
                                .getOrDefault(hostel.getHostelId(), null);

                        List<CustomerRecurringResponse> tenantList = hostelBasedCustomers.stream()
                                .map(customer -> {
                                    CustomerRecurringTracker latestTracker = latestTrackerMap
                                            .getOrDefault(customer.getCustomerId(), null);

                                    Agent trackerCreatedByAgent = null;
                                    if (latestTracker != null){
                                        String trackerCreatedBy = latestTracker.getCreatedBy();
                                        trackerCreatedByAgent = agentMap
                                                .getOrDefault(trackerCreatedBy, null);
                                    }

                                    BillingDates billingDates = null;
                                    if (hostelBillingRules != null){
                                        String billingModel = hostelBillingRules.getBillingModel();
                                        boolean isPrePaid = BillingModel.PREPAID.name().equals(billingModel);
                                        boolean isPostPaid = BillingModel.POSTPAID.name().equals(billingModel);

                                        Date joinedDate = customer.getJoiningDate() != null ? customer.getJoiningDate() : customer.getExpJoiningDate();

                                        if (joinedDate != null) {
                                            if (isPrePaid){
                                                billingDates = billingRulesService
                                                        .computeJoiningBasedBillingDates(hostelBillingRules, joinedDate, today);
                                            } else if (isPostPaid) {
                                                Date previousMonthDate = Utils.getPreviousMonthDate(today);
                                                billingDates = billingRulesService
                                                        .computeJoiningBasedBillingDates(hostelBillingRules, joinedDate, previousMonthDate);
                                            }
                                        }
                                    }

                                    return new CustomerRecurringMapper(
                                            owner,
                                            hotelType,
                                            hostel,
                                            hostelBillingRules,
                                            billingDates,
                                            latestTracker,
                                            trackerCreatedByAgent
                                    ).apply(customer);
                                }).toList();

                        return new HostelTenRecResMapper(
                                hostelBillingRules,
                                owner,
                                hotelType,
                                activeCustomersMap.getOrDefault(hostel.getHostelId(), null),
                                tenantList
                        ).apply(hostel);
                    }).toList();

            totalItems = paginatedHostels.getTotalElements();
            totalPages = paginatedHostels.getTotalPages();
            response.put("hostelList", responseList);
        } else {
            Page<Customers> paginatedCustomers = customersService
                    .getPaginatedCustomersByIds(finalFilteredCustomerIds, pageable);

            List<Customers> customers = paginatedCustomers.getContent();

            List<CustomerRecurringResponse> responseList = customers.stream()
                    .map(customer -> {
                        HostelV1 hostel = hostelMap.getOrDefault(customer.getHostelId(), null);
                        Users owner = null;
                        HotelType hotelType = null;
                        if (hostel != null) {
                            owner = ownerMap.get(hostel.getParentId());
                            hotelType = hotelTypeMap.get(hostel.getHostelType());
                        }
                        BillingRules hostelBillingRules = billingRulesMap.getOrDefault(customer.getHostelId(), null);
                        BillingDates billingDates = null;
                        if (hostelBillingRules != null){
                            String billingModel = hostelBillingRules.getBillingModel();
                            boolean isPrePaid = BillingModel.PREPAID.name().equals(billingModel);
                            boolean isPostPaid = BillingModel.POSTPAID.name().equals(billingModel);

                            Date joinedDate = customer.getJoiningDate() != null ? customer.getJoiningDate() : customer.getExpJoiningDate();

                            if (joinedDate != null) {
                                if (isPrePaid){
                                    billingDates = billingRulesService.computeJoiningBasedBillingDates(hostelBillingRules, joinedDate, today);
                                } else if (isPostPaid) {
                                    Date previousMonthDate = Utils.getPreviousMonthDate(today);
                                    billingDates = billingRulesService.computeJoiningBasedBillingDates(hostelBillingRules, joinedDate, previousMonthDate);
                                }
                            }
                        }
                        CustomerRecurringTracker latestTracker = latestTrackerMap
                                .getOrDefault(customer.getCustomerId(), null);
                        Agent trackerCreatedByAgent = null;
                        if (latestTracker != null){
                            String trackerCreatedBy = latestTracker.getCreatedBy();
                            trackerCreatedByAgent = agentMap.getOrDefault(trackerCreatedBy, null);
                        }

                        return new CustomerRecurringMapper(
                                owner,
                                hotelType,
                                hostel,
                                hostelBillingRules,
                                billingDates,
                                latestTracker,
                                trackerCreatedByAgent
                        ).apply(customer);
                    }).toList();

            totalItems = paginatedCustomers.getTotalElements();
            totalPages = paginatedCustomers.getTotalPages();
            response.put("customerList", responseList);
        }

        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        response.put("totalTenants", totalTenants);
        response.put("billingToday", billingToday);
        response.put("billingTomorrow",  billingTomorrow);
        response.put("filterOptions",  filterOptions);
        response.put("statusFilterOptions", statusFilterOptions);
        response.put("billingModelFilterOptions", billingModelFilterOptions);
        response.put("billingCycleStartDay", billingCycleStartDay);
        response.put("effectiveBillingDay", effectiveBillingDay);
        response.put("appliedFilterType", isBillingCycleFilter ? "BILLING_CYCLE" : "DATE_FILTER");
        response.put("isHostelBased", isHostelBased);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> generateTenantRecurring(List<CustomerIdPayload> payloads) {

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

        for (CustomerIdPayload payload : payloads) {

            String customerId = payload.customerId();
            if (customerId == null || customerId.isBlank()){
                return new ResponseEntity<>(Utils.CUSTOMER_ID_REQUIRED, HttpStatus.BAD_REQUEST);
            }

            Customers customer = customersService.getCustomerInformation(customerId);
            if (customer == null){
                return new ResponseEntity<>(Utils.NO_CUSTOMER_FOUND, HttpStatus.BAD_REQUEST);
            }

            String hostelId = customer.getHostelId();
            if (hostelId == null){
                return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
            }

            HostelV1 hostel = hostelRepository.findByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
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

            if (!BillingType.JOINING_DATE_BASED.name().equals(billingRules.getTypeOfBilling())){
                return new ResponseEntity<>(Utils.IS_NOT_JOINING_BASED, HttpStatus.BAD_REQUEST);
            }

            String billingModel = billingRules.getBillingModel();
            boolean isPrePaid = BillingModel.PREPAID.name().equals(billingModel);
            boolean isPostPaid = BillingModel.POSTPAID.name().equals(billingModel);

            Date joinedDate = customer.getJoiningDate() != null ? customer.getJoiningDate() : customer.getExpJoiningDate();
            if (joinedDate == null) continue;
            int billingDay = Utils.getDayOfMonth(joinedDate);

            BillingDates joinBasedBillingDates = null;
            if (isPrePaid){
                joinBasedBillingDates = billingRulesService
                        .computeJoiningBasedBillingDates(billingRules, joinedDate, today);
            } else if (isPostPaid) {
                Date previousMonthDate = Utils.getPreviousMonthDate(today);
                joinBasedBillingDates = billingRulesService
                        .computeJoiningBasedBillingDates(billingRules, joinedDate, previousMonthDate);
            }

            if (joinBasedBillingDates == null) {
                return new ResponseEntity<>(Utils.NO_BILLING_RULE_FOUND, HttpStatus.BAD_REQUEST);
            }

            Date joinBasedStartDate = joinBasedBillingDates.currentBillStartDate();

            Date cycleStart = Utils.getStartOfDay(joinBasedStartDate);
            Date tenantJoinedDate = Utils.getStartOfDay(joinedDate);

            if (isPrePaid) {
                if (!tenantJoinedDate.before(cycleStart)) {
                    return new ResponseEntity<>(Utils.INVALID_RECURRING_CYCLE_FOR_TENANT, HttpStatus.BAD_REQUEST);
                }
            }

            if (customerRecurringTrackerService.checkRecurringTrackerExists(customerId, billingDay, joinBasedStartDate)){
                return new ResponseEntity<>(Utils.RECURRING_ALREADY_CREATED, HttpStatus.BAD_REQUEST);
            }

            try {
                if (isPrePaid){
                    applicationEventPublisher.publishEvent(new JoiningBasedPrepaidEvents(this,
                            customerId, hostelId, billingDay, joinBasedBillingDates));
                } else if (isPostPaid) {
                   return new ResponseEntity<>("Postpaid joining date based recurring has not been implemented",
                           HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> getTenantRecurringHistory(String customerId, int page, int size) {

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

        Customers customer = customersService.getCustomerInformation(customerId);
        if (customer == null){
            return new ResponseEntity<>(Utils.NO_CUSTOMER_FOUND, HttpStatus.BAD_REQUEST);
        }

        HostelV1 hostel = hostelRepository.findByHostelIdAndIsActiveTrueAndIsDeletedFalse(customer.getHostelId());
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        Page<CustomerRecurringTracker> paginatedRecurringTrackers = customerRecurringTrackerService
                .getPaginatedRecurringTrackersByCustomerId(customerId, pageable);

        List<CustomerRecurringTracker> recurringTrackers = paginatedRecurringTrackers.getContent();

        Set<String> createdByIds = new HashSet<>();

        for (CustomerRecurringTracker recurringTracker : recurringTrackers) {
            if (recurringTracker.getCreatedBy() != null){
                createdByIds.add(recurringTracker.getCreatedBy());
            }
        }

        List<Agent> agents = createdByIds.isEmpty()
                ? Collections.emptyList()
                : agentService.getAgentsByIds(createdByIds);

        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId,
                        agent1 -> agent1));

        List<CustomerRecHistoryRes> recurringHistory = recurringTrackers.stream()
                .map(recurringTracker -> new CustomerRecHistoryMapper(
                        customer, hostel, agentMap.getOrDefault(recurringTracker.getCreatedBy(), null)
                ).apply(recurringTracker)).toList();

        List<HotelType> hotelTypes = hotelTypeService.getAllHotelTypes();
        Map<Integer, HotelType> hotelTypeMap = hotelTypes.stream()
                .collect(Collectors.toMap(HotelType::getId, hotelType -> hotelType));

        Users owner = usersService.getOwner(hostel.getParentId());

        CustomerRecurringTracker latestRecurringTracker = customerRecurringTrackerService
                .getLatestTrackersByCustomerId(customerId);

        BillingRules billingRules = billingRulesService.getCurrentMonthTemplate(customer.getHostelId());

        if (billingRules == null){
            return new ResponseEntity<>(Utils.NO_BILLING_RULE_FOUND, HttpStatus.BAD_REQUEST);
        }

        String billingModel = billingRules.getBillingModel();
        boolean isPrePaid = BillingModel.PREPAID.name().equals(billingModel);
        boolean isPostPaid = BillingModel.POSTPAID.name().equals(billingModel);

        BillingDates billingDates = null;
        Date today = new Date();
        Date joinedDate = customer.getJoiningDate() != null ? customer.getJoiningDate() : customer.getExpJoiningDate();

        if (isPrePaid){
            billingDates = billingRulesService.computeJoiningBasedBillingDates(billingRules, joinedDate, today);
        } else if (isPostPaid) {
            Date previousMonthDate = Utils.getPreviousMonthDate(today);
            billingDates = billingRulesService.computeJoiningBasedBillingDates(billingRules, joinedDate, previousMonthDate);
        }

        CustomerRecTrackerRes recurringTrackerRes = new CustomerRecTrackerResMapper(
                owner,
                hotelTypeMap.get(hostel.getHostelType()),
                hostel,
                billingRules,
                billingDates,
                latestRecurringTracker,
                page + 1,
                size,
                paginatedRecurringTrackers,
                recurringHistory
        ).apply(customer);

        return new ResponseEntity<>(recurringTrackerRes, HttpStatus.OK);
    }
}
