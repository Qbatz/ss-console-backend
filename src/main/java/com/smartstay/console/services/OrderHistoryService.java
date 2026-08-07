package com.smartstay.console.services;

import com.smartstay.console.Mapper.orderHistory.OrderHistoryMapper;
import com.smartstay.console.config.*;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.orderHistory.PaymentLinkGenerateDto;
import com.smartstay.console.dto.orderHistory.PaymentLinkGenerateResDto;
import com.smartstay.console.dto.subscription.SubscriptionSnapshot;
import com.smartstay.console.ennum.*;
import com.smartstay.console.payloads.orderHistory.PaymentLinkGeneratePayload;
import com.smartstay.console.payloads.orderHistory.PaymentLinkSharePayload;
import com.smartstay.console.repositories.OrderHistoryRepository;
import com.smartstay.console.responses.orderHistory.*;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderHistoryService {

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private Authentication  authentication;
    @Autowired
    private HotelTypeService hotelTypeService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private PlansService plansService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private WhatsappService whatsappService;
    @Autowired
    @Lazy
    private SubscriptionService subscriptionService;
    @Autowired
    private UploadFileToS3 uploadFileToS3;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private S3Service s3Service;

    @Value("${PAYMENT_URL}")
    private String paymentUrl;

    private final RestTemplate restTemplate;

    public OrderHistoryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

    public ResponseEntity<?> getOrderHistory(int page, int size, String name, Date startDate, Date endDate) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Payments.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Set<String> filteredHostelIds = new HashSet<>();
        Set<String> filteredUserIds = new HashSet<>();

        if (name != null && !name.isBlank()) {
            List<HostelV1> filteredHostels = hostelService.getHostelsByHostelName(name.trim());
            filteredHostelIds = filteredHostels.stream()
                    .map(HostelV1::getHostelId)
                    .collect(Collectors.toSet());

            List<Users> filteredUsers = usersService.getUsersByName(name.trim());
            filteredUserIds = filteredUsers.stream()
                    .map(Users::getUserId)
                    .collect(Collectors.toSet());
        }

        LocalDate today = LocalDate.now();

        if (startDate == null) {
            startDate = Utils.getStartDateOfMonth(today);
        }
        if (endDate == null) {
            endDate = Utils.getEndDateOfMonth(today);
        }
        endDate = Utils.addDaysToDate(endDate, 1);

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        List<String> orderStatuses = List.of(OrderStatus.PAID.name());

        double totalRevenue = orderHistoryRepository
                .findTotalRevenueBetween(startDate, endDate, orderStatuses);
        totalRevenue = Utils.roundOfDoubleTo2Digits(totalRevenue);

        if (name != null && !name.trim().isEmpty()
                && filteredHostelIds.isEmpty() && filteredUserIds.isEmpty()) {

            OrderHistoryPagedResponse response = new OrderHistoryPagedResponse(totalRevenue, page + 1,
                    size, 0, 0, Collections.emptyList(), null, null);

            return ResponseEntity.ok(response);
        }

        Page<OrderHistory> paginatedAllOrderHistory;
        Page<OrderHistory> paginatedPaidOrderHistory;
        Page<OrderHistory> paginatedCreatedOrderHistory;

        String paidOrderStatus = OrderStatus.PAID.name();
        String createdOrderStatus = OrderStatus.CREATED.name();

        if (!filteredHostelIds.isEmpty() || !filteredUserIds.isEmpty()) {

            filteredHostelIds = filteredHostelIds.isEmpty() ? null : filteredHostelIds;
            filteredUserIds = filteredUserIds.isEmpty() ? null : filteredUserIds;

            paginatedAllOrderHistory = orderHistoryRepository
                    .findFilteredOrderHistory(filteredHostelIds, filteredUserIds, startDate, endDate,
                            pageable);
            paginatedPaidOrderHistory = orderHistoryRepository
                    .findStatusFilteredOrderHistory(filteredHostelIds, filteredUserIds, startDate, endDate,
                            paidOrderStatus, pageable);
            paginatedCreatedOrderHistory = orderHistoryRepository
                    .findStatusFilteredOrderHistory(filteredHostelIds, filteredUserIds, startDate, endDate,
                            createdOrderStatus, pageable);

        } else {
            paginatedAllOrderHistory = orderHistoryRepository
                    .findAllByPaidOrCreatedDate(startDate, endDate, pageable);
            paginatedPaidOrderHistory = orderHistoryRepository
                    .findStatusAllByPaidOrCreatedDate(startDate, endDate, paidOrderStatus, pageable);
            paginatedCreatedOrderHistory = orderHistoryRepository
                    .findStatusAllByPaidOrCreatedDate(startDate, endDate, createdOrderStatus, pageable);
        }

        List<OrderHistory> allOrderHistories = paginatedAllOrderHistory.getContent();
        List<OrderHistory> paidOrderHistories = paginatedPaidOrderHistory.getContent();
        List<OrderHistory> createdOrderHistories = paginatedCreatedOrderHistory.getContent();

        List<OrderHistory> orderHistories = new ArrayList<>();
        orderHistories.addAll(allOrderHistories);
        orderHistories.addAll(paidOrderHistories);
        orderHistories.addAll(createdOrderHistories);

        Set<Long> historyIds = new HashSet<>();
        Set<String> hostelIds = new HashSet<>();
        Set<String> planCodes = new HashSet<>();
        Set<String> userIds = new HashSet<>();
        Set<String> agentIds = new HashSet<>();

        for (OrderHistory orderHistory : orderHistories) {
            if (orderHistory.getHistoryId() != null) {
                historyIds.add(orderHistory.getHistoryId());
            }
            if (orderHistory.getHostelId() != null) {
                hostelIds.add(orderHistory.getHostelId());
            }
            if (orderHistory.getPlanCode() != null) {
                planCodes.add(orderHistory.getPlanCode());
            }
            if (orderHistory.getPaidBy() != null) {
                userIds.add(orderHistory.getPaidBy());
            }
            if (orderHistory.getCollectedBy() != null) {
                agentIds.add(orderHistory.getCollectedBy());
            }
            if (orderHistory.getCreatedBy() != null && orderHistory.getUserType() != null) {
                if (UserType.OWNER.name().equals(orderHistory.getUserType())){
                    userIds.add(orderHistory.getCreatedBy());
                } else if (UserType.AGENT.name().equals(orderHistory.getUserType())) {
                    agentIds.add(orderHistory.getCreatedBy());
                }
            }
        }

        List<Subscription> subscriptions = subscriptionService
                .getSubscriptionsByOrderIds(historyIds);
        Map<Long, Subscription> subscriptionMap = subscriptions.stream()
                .collect(Collectors.toMap(Subscription::getOrderId, s -> s,
                        (a, b) -> a));

        List<HotelType> hotelTypes = hotelTypeService.getAllHotelTypes();
        Map<Integer, HotelType> hotelTypeMap = hotelTypes.stream()
                .collect(Collectors.toMap(HotelType::getId, hotelType -> hotelType));

        List<HostelV1> hostels = hostelIds.isEmpty() ? Collections.emptyList() : hostelService.getHostelsByHostelIds(hostelIds);
        Map<String, HostelV1> hostelMap = hostels.stream()
                .collect(Collectors.toMap(HostelV1::getHostelId, hostel -> hostel, (a, b) -> a));

        List<Plans> plans = planCodes.isEmpty() ? Collections.emptyList() : plansService.findPlansByPlanCodes(planCodes);
        Map<String, Plans> plansMap = plans.stream()
                .collect(Collectors.toMap(Plans::getPlanCode, plan -> plan, (a, b) -> a));

        List<Users> usersList = userIds.isEmpty() ? Collections.emptyList() : usersService.getUsersByIds(userIds);
        Map<String, Users> usersMap = usersList.stream()
                .collect(Collectors.toMap(Users::getUserId, user -> user, (a, b) -> a));

        List<Agent> agents = agentIds.isEmpty() ? Collections.emptyList() : agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a, (a, b) -> a));

        Set<String> parentIds = hostels.stream()
                .map(HostelV1::getParentId)
                .collect(Collectors.toSet());
        List<Users> owners = parentIds.isEmpty() ? Collections.emptyList() : usersService.getOwners(new ArrayList<>(parentIds));
        Map<String, Users> ownerMap = owners.stream()
                .collect(Collectors.toMap(Users::getParentId, user -> user, (a, b) -> a));

        List<OrderHistoryResponse> orderHistoriesRes = mapOrderHistories(allOrderHistories,
                hostelMap, hotelTypeMap, plansMap, usersMap, agentMap, ownerMap, subscriptionMap);

        List<OrderHistoryResponse> paidOrderHistoriesRes = mapOrderHistories(paidOrderHistories,
                hostelMap, hotelTypeMap, plansMap, usersMap, agentMap, ownerMap, subscriptionMap);

        List<OrderHistoryResponse> createdOrderHistoriesRes = mapOrderHistories(createdOrderHistories,
                hostelMap, hotelTypeMap, plansMap, usersMap, agentMap, ownerMap, subscriptionMap);

        StatusOrderHistoryPagedResponse paidHistories = new StatusOrderHistoryPagedResponse(
                page + 1, size, paginatedPaidOrderHistory.getTotalElements(),
                paginatedPaidOrderHistory.getTotalPages(), paidOrderHistoriesRes
        );

        StatusOrderHistoryPagedResponse createdHistories = new StatusOrderHistoryPagedResponse(
                page + 1, size, paginatedCreatedOrderHistory.getTotalElements(),
                paginatedCreatedOrderHistory.getTotalPages(), createdOrderHistoriesRes
        );

        OrderHistoryPagedResponse response = new OrderHistoryPagedResponse(
                totalRevenue, page + 1, size, paginatedAllOrderHistory.getTotalElements(),
                paginatedAllOrderHistory.getTotalPages(), orderHistoriesRes, paidHistories,
                createdHistories
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<OrderHistoryResponse> mapOrderHistories(List<OrderHistory> orderHistories,
                                                         Map<String, HostelV1> hostelMap,
                                                         Map<Integer, HotelType> hotelTypeMap,
                                                         Map<String, Plans> plansMap,
                                                         Map<String, Users> usersMap,
                                                         Map<String, Agent> agentMap,
                                                         Map<String, Users> ownerMap,
                                                         Map<Long, Subscription> subscriptionMap) {

        return orderHistories.stream()
                .map(orderHistory -> {
                    HostelV1 hostel = hostelMap.getOrDefault(orderHistory.getHostelId(), null);
                    Plans plan = plansMap.getOrDefault(orderHistory.getPlanCode(), null);
                    HotelType hotelType = null;
                    Users owner = null;
                    if (hostel != null) {
                        hotelType = hotelTypeMap.getOrDefault(hostel.getHostelType(), null);
                        owner = ownerMap.getOrDefault(hostel.getParentId(), null);
                    }
                    Subscription subscription = subscriptionMap.getOrDefault(orderHistory.getHistoryId(), null);
                    return new OrderHistoryMapper(hostel, hotelType, plan, usersMap, agentMap,
                            owner, subscription).apply(orderHistory);
                }).toList();
    }

    public OrderHistory save(OrderHistory newOrder) {
        return orderHistoryRepository.save(newOrder);
    }

    public ResponseEntity<?> verifyOrderHistory(Long orderHistoryId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Payments.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        OrderHistory orderHistory = orderHistoryRepository.findByHistoryIdAndIsActiveTrue(orderHistoryId);
        if (orderHistory == null){
            return new ResponseEntity<>(Utils.ORDER_HISTORY_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        String paymentId = orderHistory.getPaymentId();

        if (paymentId == null) {
            return new ResponseEntity<>(Utils.PAYMENT_ID_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        VerifyResponse verifyResponse;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String verifyPaymentUrl = paymentUrl + "/v2/payments/" + paymentId ;

            restTemplate.exchange(
                    verifyPaymentUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            verifyResponse = new VerifyResponse(true, "SUCCESS");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            verifyResponse = new VerifyResponse(false, "FAILED");
        } catch (Exception e){
            return new ResponseEntity<>(Utils.UNABLE_TO_VERIFY_PAYMENT, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(verifyResponse, HttpStatus.OK);
    }

    public ResponseEntity<?> generatePaymentLink(String hostelId, PaymentLinkGeneratePayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Payments.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelService.getHostelByHostelId(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        String paidBy = payload.paidBy();
        Users users = usersService.getUserById(paidBy);
        if (users == null){
            return new ResponseEntity<>(Utils.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (!hostel.getParentId().equals(users.getParentId())){
            return new ResponseEntity<>(Utils.PAID_BY_HOSTEL_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        String planCode = payload.planCode();
        Plans plan = plansService.findPlanByPlanCode(planCode);
        if (plan == null){
            return new ResponseEntity<>(Utils.PLAN_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (PlanType.TRIAL.name().equals(plan.getPlanType()) ||
                PlanType.EXPANDABLE_TRIAL.name().equals(plan.getPlanType())) {
            return new ResponseEntity<>(Utils.INVALID_PLAN_CODE, HttpStatus.BAD_REQUEST);
        }

        double finalPrice = plan.getFinalPrice() != null ? plan.getFinalPrice() : 0;
        double discountAmount;

        try {
            discountAmount = Double.parseDouble(payload.discountAmount().toString());
        }
        catch (Exception e) {
            discountAmount = 0.0;
        }
        discountAmount = Utils.roundOfDoubleTo2Digits(discountAmount);

        if (discountAmount < 0 || discountAmount > finalPrice) {
            return new ResponseEntity<>(Utils.INVALID_DISCOUNT, HttpStatus.BAD_REQUEST);
        }

        double payableAmount = finalPrice - discountAmount;
        payableAmount = Utils.roundOfDoubleTo2Digits(payableAmount);

        try {
            String generatePaymentLink = paymentUrl + "/v2/payments/generate/" + hostelId ;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            PaymentLinkGenerateDto requestPayload = new PaymentLinkGenerateDto(payableAmount, "INR",
                    null, planCode, discountAmount, finalPrice, agent.getAgentId());

            HttpEntity<PaymentLinkGenerateDto> request =
                    new HttpEntity<>(requestPayload, headers);

            ResponseEntity<PaymentLinkGenerateResDto> response = restTemplate.exchange(
                    generatePaymentLink,
                    HttpMethod.POST,
                    request,
                    PaymentLinkGenerateResDto.class
            );

            PaymentLinkGenerateResDto responseBody = response.getBody();

            if (responseBody == null) {
                return new ResponseEntity<>(Utils.UNABLE_TO_GENERATE_PAYMENT_LINK, HttpStatus.BAD_REQUEST);
            }

            OrderHistory newOrder = new OrderHistory();
            newOrder.setHostelId(hostelId);
            newOrder.setPaymentUrl(responseBody.paymentLink());
            newOrder.setPaymentId(responseBody.paymentLinkId());
            newOrder.setPaymentLinkId(responseBody.paymentLinkId());
            newOrder.setDiscountAmount(discountAmount);
            newOrder.setPlanAmount(finalPrice);
            newOrder.setPlanCode(planCode);
            newOrder.setPlanName(plan.getPlanName());
            newOrder.setTotalAmount(payableAmount);
            newOrder.setOrderStatus(OrderStatus.CREATED.name());
            newOrder.setUserType(UserType.AGENT.name());
            newOrder.setPaidBy(paidBy);
            newOrder.setActive(true);
            newOrder.setCreatedAt(new Date());
            newOrder.setCreatedBy(agent.getAgentId());

            orderHistoryRepository.save(newOrder);

            GeneratePaymentLinkRes paymentLinkRes = new GeneratePaymentLinkRes(responseBody.paymentLink());

            return new ResponseEntity<>(paymentLinkRes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Utils.UNABLE_TO_GENERATE_PAYMENT_LINK, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> sharePaymentLinkToWhatsapp(String hostelId, PaymentLinkSharePayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Payments.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelService.getHostelByHostelId(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        String paymentLink = payload.paymentLink();

        OrderHistory orderHistory = orderHistoryRepository
                .findByPaymentUrlAndOrderStatusAndIsActiveTrue(paymentLink, OrderStatus.CREATED.name());
        if (orderHistory == null){
            return new ResponseEntity<>(Utils.ORDER_HISTORY_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!orderHistory.getHostelId().equals(hostelId)){
            return new ResponseEntity<>(Utils.PAYMENT_URL_AND_HOSTEL_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        String parentId = hostel.getParentId();

        Users owner = usersService.getOwner(parentId);
        if (owner == null){
            return new ResponseEntity<>(Utils.NO_OWNER_FOUND, HttpStatus.BAD_REQUEST);
        }

        String ownerName = Utils.getFullName(owner.getFirstName(), owner.getLastName());
        String ownerMobile = owner.getMobileNo();

        whatsappService.sendPaymentLink(ownerName, ownerMobile, paymentLink);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> uploadInvoice(Long orderHistoryId, MultipartFile invoice, Boolean isManual) {

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Payments.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        OrderHistory orderHistory = orderHistoryRepository.findByHistoryIdAndIsActiveTrue(orderHistoryId);
        if (orderHistory == null){
            return new ResponseEntity<>(Utils.ORDER_HISTORY_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<Subscription> subscriptions = subscriptionService.getSubscriptionByOrderId(orderHistoryId);
        if (subscriptions.isEmpty()){
            return new ResponseEntity<>(Utils.SUBSCRIPTION_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (subscriptions.size() > 1){
            return new ResponseEntity<>("Multiple subscription exists for this order", HttpStatus.BAD_REQUEST);
        }

        Subscription subscription = subscriptions.getFirst();
        if (subscription == null){
            return new ResponseEntity<>(Utils.SUBSCRIPTION_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (subscription.getInvoiceUrl() != null){
            return new ResponseEntity<>("Invoice already exists, delete first", HttpStatus.BAD_REQUEST);
        }

        SubscriptionSnapshot oldSnapshot = SnapshotUtility.toSnapshot(subscription);

        String invoiceUrl = null;
        try {
            invoiceUrl = uploadFileToS3.uploadFileToS3(
                    FilesConfig.convertMultipartToFileNew(invoice), "subscription/invoice");
        } catch (Exception e) {
            return new ResponseEntity<>(Utils.FILE_UPLOAD_FAILED, HttpStatus.BAD_REQUEST);
        }

        if (isManual == null){
            isManual = true;
        }

        String generationType = null;
        if (isManual){
            generationType = GenerationType.MANUAL.name();
        } else {
            generationType = GenerationType.AUTOMATIC.name();
        }

        subscription.setInvoiceUrl(invoiceUrl);
        subscription.setGenerationType(generationType);

        subscription = subscriptionService.save(subscription);

        SubscriptionSnapshot newSnapshot = SnapshotUtility.toSnapshot(subscription);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.SUBSCRIPTION_INVOICE_URL,
                String.valueOf(subscription.getSubscriptionId()), oldSnapshot, newSnapshot);

        return new ResponseEntity<>(Utils.FILE_UPLOAD_SUCCESS, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteInvoice(Long orderHistoryId)  {

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Payments.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        OrderHistory orderHistory = orderHistoryRepository.findByHistoryIdAndIsActiveTrue(orderHistoryId);
        if (orderHistory == null){
            return new ResponseEntity<>(Utils.ORDER_HISTORY_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<Subscription> subscriptions = subscriptionService.getSubscriptionByOrderId(orderHistoryId);
        if (subscriptions.isEmpty()){
            return new ResponseEntity<>(Utils.SUBSCRIPTION_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (subscriptions.size() > 1){
            return new ResponseEntity<>("Multiple subscription exists for this order", HttpStatus.BAD_REQUEST);
        }

        Subscription subscription = subscriptions.getFirst();
        if (subscription == null){
            return new ResponseEntity<>(Utils.SUBSCRIPTION_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (subscription.getInvoiceUrl() == null){
            return new ResponseEntity<>(Utils.SUBSCRIPTION_INVOICE_URL_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (GenerationType.MANUAL.name().equals(subscription.getGenerationType())){
            return new ResponseEntity<>("Manual invoice can not be deleted", HttpStatus.BAD_REQUEST);
        }

        SubscriptionSnapshot oldSnapshot = SnapshotUtility.toSnapshot(subscription);

        try {
            s3Service.deleteFile(subscription.getInvoiceUrl());
        } catch (Exception e) {
//            return new ResponseEntity<>("Failed to delete invoice file from S3",
//                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        subscription.setInvoiceUrl(null);

        subscription = subscriptionService.save(subscription);

        agentActivitiesService.createAgentActivity(agent, ActivityType.DELETE, Source.SUBSCRIPTION_INVOICE_URL,
                String.valueOf(subscription.getSubscriptionId()), oldSnapshot, null);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }
}
