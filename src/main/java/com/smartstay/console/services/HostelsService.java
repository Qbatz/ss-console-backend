package com.smartstay.console.services;

import com.smartstay.console.Mapper.customers.CustomerResMapper;
import com.smartstay.console.Mapper.hostels.HostelDetailsMapper;
import com.smartstay.console.Mapper.hostels.HostelsListMapper;
import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.Mapper.users.UsersResponseMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.ennum.BankSource;
import com.smartstay.console.ennum.BookingsStatus;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.payloads.hostel.HostelIdPayload;
import com.smartstay.console.repositories.HostelV1Repositories;
import com.smartstay.console.responses.customers.CustomerResponse;
import com.smartstay.console.responses.hostels.*;
import com.smartstay.console.responses.users.UsersResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    public ResponseEntity<?> getAllHostels(int page, int size, String hostelName) {
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
        if (page > 0) {
            page = page - 1;
        }

        long totalHostels = hostelRepository.findHostelCount();

        int currentPage = page;

        List<HostelPlan> actHostels = hostelPlansService.findActiveHostels();


        List<HostelV1> allHostels = hostelRepository.findAllHostels(size, page*size, hostelName);

        List<HostelV1> hostelsFromPlan = allHostels
                .stream()
                .toList();

        List<String> parentId = hostelsFromPlan
                .stream()
                .map(HostelV1::getParentId)
                .toList();
        List<String> hostelIds = hostelsFromPlan
                .stream()
                .map(HostelV1::getHostelId)
                .toList();

        List<Users> createdUsers = usersService.getOwners(parentId);
        List<OwnerInfo> ownerInfos = createdUsers
                .stream()
                .map(i -> new UserOnerInfoMapper().apply(i))
                .toList();
        List<UserActivities> listActivities = userActivitiesService.findLatestActivities(hostelIds);

        List<HostelList> hostelsList = hostelsFromPlan
                .stream()
                .map(i -> new HostelsListMapper(ownerInfos, listActivities).apply(i))
                .toList();

        long inactiveHostels = totalHostels - actHostels.size();

        Hostels hostels = new Hostels(totalHostels,
                actHostels.size(),
                inactiveHostels,
                currentPage,
                size,
                hostelsList);

        return new ResponseEntity<>(hostels, HttpStatus.OK);

    }

    public List<HostelV1> getHostelsByParentIds(List<String> parentIds) {
        return hostelRepository.findAllByParentIdIn(parentIds);
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

        List<UserActivities> activities = userActivitiesService.getActivitiesByHostelId(hostelId);

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
        List<CustomersAmenity> listCustomersAmenity = customersAmenityService.findByHostelIdAndCustomerIdIs(customerIds);
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
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
