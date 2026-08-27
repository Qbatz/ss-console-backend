package com.smartstay.console.services;

import com.smartstay.console.Mapper.kyc.KycHostelResMapper;
import com.smartstay.console.Mapper.kyc.KycTenantResMapper;
import com.smartstay.console.Mapper.kycDetails.KycDetailsResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.config.FilesConfig;
import com.smartstay.console.config.UploadFileToS3;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.customers.KycDetailsSnapshot;
import com.smartstay.console.dto.date.StartEndDateDto;
import com.smartstay.console.dto.files.UploadFiles;
import com.smartstay.console.dto.kycDetails.*;
import com.smartstay.console.ennum.*;
import com.smartstay.console.exceptions.BadRequestException;
import com.smartstay.console.repositories.KycDetailsRepository;
import com.smartstay.console.responses.date.DateFilterRes;
import com.smartstay.console.responses.kyc.KycHostelRes;
import com.smartstay.console.responses.kyc.KycStatusResponse;
import com.smartstay.console.responses.kyc.KycTenantRes;
import com.smartstay.console.responses.kycDetails.KycDetailsRes;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KycDetailsService {

    @Value("${DIGIO_URL}")
    private String digioUrl;
    @Value("${DIGIO_REQUEST_URL}")
    private String digioRequestUrl;
    @Value("${DIGIO_MEDIA_URL}")
    private String digioMediaUrl;
    @Value("${DIGIO_USERNAME}")
    private String digioUserName;
    @Value("${DIGIO_PASSWORD}")
    private String digioPassword;

    private final RestTemplate restTemplate;

    public KycDetailsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    private KycDetailsRepository kycDetailsRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private UploadFileToS3 uploadFileToS3;
    @Autowired
    private KycUsageService kycUsageService;
    @Autowired
    private BillingRulesService billingRulesService;
    @Autowired
    private CustomerNotificationsService customerNotificationsService;
    @Autowired
    private UsersService usersService;

    public ResponseEntity<?> getWaitingApprovalKycDetails(int page, int size, String name) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        Set<String> customerIds = null;
        if (name != null && !name.isBlank()){
            List<Customers> customers = customersService.getCustomersByName(name.trim());

            customerIds = customers.stream()
                    .map(Customers::getCustomerId)
                    .collect(Collectors.toSet());

            if (customerIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("tenantList", List.of());
                response.put("currentPage", page + 1);
                response.put("pageSize", size);
                response.put("totalItems", 0);
                response.put("totalPages", 0);

                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        }

        Page<KycDetails> pagedKycDetails = kycDetailsRepository.
                findPaginatedKycDetailsAndKycStatusIn(customerIds,
                        KycStatus.WAITING_FOR_APPROVAL.name(), pageable);

        List<KycDetails> kycDetailsList = pagedKycDetails.getContent();

        List<Customers> customersList = kycDetailsList.stream()
                .map(KycDetails::getCustomers)
                .toList();

        Set<String> hostelIds = customersList.stream()
                .map(Customers::getHostelId)
                .collect(Collectors.toSet());

        List<HostelV1> hostels = hostelService.getHostelsByHostelIds(hostelIds);

        Map<String, HostelV1> hostelMap = hostels.stream()
                .collect(Collectors.toMap(HostelV1::getHostelId, hostel -> hostel));

        List<KycDetailsRes> responseList = kycDetailsList.stream()
                .map(kycDetails -> {
                    Customers customer = kycDetails.getCustomers();
                    String hostelId = customer != null ? customer.getHostelId() : null;
                    HostelV1 hostel = hostelMap.getOrDefault(hostelId, null);

                    return new KycDetailsResMapper(hostel)
                            .apply(kycDetails);
                }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("tenantList", responseList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedKycDetails.getTotalElements());
        response.put("totalPages", pagedKycDetails.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> approveKycRequest(String customerId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Customers customer = customersService.getCustomerInformation(customerId);
        if (customer == null) {
            return new ResponseEntity<>(Utils.NO_CUSTOMER_FOUND, HttpStatus.BAD_REQUEST);
        }

        KycDetails kycDetails = customer.getKycDetails();
        if (kycDetails == null) {
            return new ResponseEntity<>(Utils.KYC_DETAILS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        KycDetailsSnapshot oldSnapshot = SnapshotUtility.toSnapshot(kycDetails);

        if (KycStatus.VERIFIED.name().equalsIgnoreCase(kycDetails.getCurrentStatus())) {
            return new ResponseEntity<>("Kyc already verified", HttpStatus.OK);
        }

        if (!KycStatus.WAITING_FOR_APPROVAL.name().equalsIgnoreCase(kycDetails.getCurrentStatus())){
            return new ResponseEntity<>("This tenant does not have an approval in waiting", HttpStatus.BAD_REQUEST);
        }

        String digioVerifyUrl = digioUrl + kycDetails.getEntityId() + "/response";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(digioUserName, digioPassword);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>("{}", headers);

            ResponseEntity<DigioKycResponse> response = restTemplate.exchange(
                    digioVerifyUrl,
                    HttpMethod.POST,
                    request,
                    DigioKycResponse.class
            );

            DigioKycResponse digioKycResponse = response.getBody();
            if (digioKycResponse == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No response body found");
            }

            String status = digioKycResponse.status();

            if (response.getStatusCode() == HttpStatus.OK) {
                if (status != null){
                    if (KycStatus.APPROVED.name().equalsIgnoreCase(status)) {
                        buildKycDetails(kycDetails, digioKycResponse, customerId);
                        kycDetails = kycDetailsRepository.save(kycDetails);

                        KycDetailsSnapshot newSnapshot = SnapshotUtility.toSnapshot(kycDetails);

                        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.KYC_DETAILS,
                                String.valueOf(kycDetails.getId()), oldSnapshot, newSnapshot);
                    } else if (KycStatus.REQUESTED.name().equalsIgnoreCase(status)) {
                        return new ResponseEntity<>("Can not approve requested kyc status", HttpStatus.BAD_REQUEST);
                    } else {
                        return manageApproval(kycDetails, customerId, agent, oldSnapshot);
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No status found");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request");
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Server error");
        }

        return new ResponseEntity<>(Utils.APPROVED, HttpStatus.OK);
    }

    private void buildKycDetails(KycDetails kycDetails, DigioKycResponse digioKycResponse,
                                 String customerId) {

        Date today = new Date();

        kycDetails.setCurrentStatus(KycStatus.VERIFIED.name());
        kycDetails.setUpdatedAt(today);

        List<DigioKycAction> actions = digioKycResponse.actions() != null
                ? digioKycResponse.actions() : new ArrayList<>();

        if (!actions.isEmpty()) {
            DigioKycAction action = actions.getFirst();

            if (action != null) {
                kycDetails.setCompletedAt(Utils.stringDateToDate(action.completedAt()));

                if (action.executionRequestId() != null){

                    UploadFiles uploadFiles = getKycDocument(customerId, action.executionRequestId());

                    if (uploadFiles != null) {
                        kycDetails.setKycDocumentType(FileFormat.PDF.name());
                        //kycDetails.setKycDocumentType(uploadFiles.fileFormat());
                        kycDetails.setKycDocument(uploadFiles.fileName());
                    }
                }

                DigioKycDetails digioKycDetails = action.details();

                if (digioKycDetails != null) {
                    DigioKycAadhaarDetails aadhaarDetails = digioKycDetails.aadhaarDetails();

                    if (aadhaarDetails != null) {
                        kycDetails.setAadhaarNumber(aadhaarDetails.idNumber());
                        kycDetails.setDateOfBirth(aadhaarDetails.dateOfBirth());
                        kycDetails.setDocumentType(KycDocumentType.AADHAAR.name());
                        if (aadhaarDetails.gender().equalsIgnoreCase("F")) {
                            kycDetails.setGender("Female");
                        }
                        else if (aadhaarDetails.gender().equalsIgnoreCase("M")) {
                            kycDetails.setGender("Male");
                        }
                        else {
                            kycDetails.setGender("Others");
                        }
                        String aadhaarImage = uploadFileToS3
                                .uploadFileToS3(FilesConfig
                                        .base64ToImage(customerId,
                                                aadhaarDetails.image()), "kyc-pic");
                        kycDetails.setIdPic(aadhaarImage);
                        kycDetails.setNameInDocument(aadhaarDetails.name());
                        kycDetails.setPermanentAddress(aadhaarDetails.permanentAddressString());

                        DigioKycAddressDetails currentAddress = aadhaarDetails.currentAddressDetails();
                        DigioKycAddressDetails permanentAddress = aadhaarDetails.permanentAddress();

                        KycAddressDetails kycAddressDetails = kycDetails.getAddressDetails();
                        if (kycAddressDetails == null) {
                            kycAddressDetails = new KycAddressDetails();
                        }

                        if (currentAddress != null) {
                            kycAddressDetails.setCurrentCity(currentAddress.districtOrCity());
                            kycAddressDetails.setCurrentAddress(currentAddress.address());
                            kycAddressDetails.setCurrentLocality(currentAddress.localityOrPostOffice());
                            kycAddressDetails.setCurrentPincode(currentAddress.pincode());
                            kycAddressDetails.setCurrentState(currentAddress.state());
                        }

                        if (permanentAddress != null) {
                            kycAddressDetails.setPermanentCity(permanentAddress.districtOrCity());
                            kycAddressDetails.setPermanentAddress(permanentAddress.address());
                            kycAddressDetails.setPermanentLocality(permanentAddress.localityOrPostOffice());
                            kycAddressDetails.setPermanentPincode(permanentAddress.pincode());
                            kycAddressDetails.setPermanentState(permanentAddress.state());
                        }

                        kycAddressDetails.setKycDetails(kycDetails);
                        kycDetails.setAddressDetails(kycAddressDetails);
                    }
                }
            }
        }
    }

    private ResponseEntity<?> manageApproval(KycDetails kycDetails, String customerId,
                                             Agent agent, KycDetailsSnapshot oldSnapshot) {

        try {
            String digioApprovalRequestUrl = digioRequestUrl + kycDetails.getEntityId() + "/manage_approval";

            HttpHeaders approvalRequestHeaders = new HttpHeaders();
            approvalRequestHeaders.setBasicAuth(digioUserName, digioPassword);
            approvalRequestHeaders.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> approvalBody = new HashMap<>();
            approvalBody.put("status", "approved");

            HttpEntity<Map<String, String>> approvalRequest = new HttpEntity<>(approvalBody, approvalRequestHeaders);

            ResponseEntity<DigioKycResponse> approvalResponse = restTemplate.exchange(
                    digioApprovalRequestUrl,
                    HttpMethod.POST,
                    approvalRequest,
                    DigioKycResponse.class
            );

            DigioKycResponse approvalDigioKycResponse = approvalResponse.getBody();
            if (approvalDigioKycResponse == null) {
                throw new BadRequestException("No response body found");
            }

            String approvalStatus = approvalDigioKycResponse.status();

            if (approvalResponse.getStatusCode() == HttpStatus.OK) {
                if (approvalStatus != null){

                    if (KycStatus.APPROVED.name().equalsIgnoreCase(approvalStatus)) {
                        buildKycDetails(kycDetails, approvalDigioKycResponse, customerId);
                        kycDetails = kycDetailsRepository.save(kycDetails);

                        KycDetailsSnapshot newSnapshot = SnapshotUtility.toSnapshot(kycDetails);

                        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.KYC_DETAILS,
                                String.valueOf(kycDetails.getId()), oldSnapshot, newSnapshot);
                    }

                    return new ResponseEntity<>(Utils.APPROVED, HttpStatus.OK);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No status found");
                }
            } else {
                return new ResponseEntity<>("Invalid approval", HttpStatus.BAD_REQUEST);
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            return new ResponseEntity<>("Server error", HttpStatus.BAD_REQUEST);
        }
    }

    private UploadFiles getKycDocument(String customerId, String executionRequestId) {

        try {
            String digioDocumentUrl = digioMediaUrl + executionRequestId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(digioUserName, digioPassword);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(headers);

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(digioDocumentUrl)
                    .queryParam("doc_type", "AADHAAR")
                    .queryParam("base64", true);

            ResponseEntity<DigioKycMediaResponse> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    request,
                    DigioKycMediaResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                DigioKycMediaResponse mediaResponse = response.getBody();
                if (mediaResponse != null && mediaResponse.file() != null) {
                    byte[] pdfBytes = Base64.getDecoder().decode(mediaResponse.file().getBytes());

                    File aadhaarPdf = FilesConfig.writePdf(pdfBytes, customerId);
                    return uploadFileToS3.uploadFilesToS3(aadhaarPdf, "kyc-docs");
                }
            }

            return null;
        }  catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new BadRequestException("Server error");
        }
    }

    public ResponseEntity<?> getHostels(int page, int size, String name, Boolean isEnabled,
                                        String dateFilter, Date startDate, Date endDate) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        name = (name == null || name.isBlank()) ? null : name.trim();

        DateFilterEnum filter;
        try {
            filter = DateFilterEnum.valueOf(dateFilter);
        } catch (Exception e) {
            return new ResponseEntity<>(Utils.DATE_FILTER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<DateFilterRes> dateFilters = Arrays.stream(DateFilterEnum.values())
                .map(i -> new DateFilterRes(i.name(), i.getValue()))
                .toList();

        StartEndDateDto dateRange = Utils.getDateRange(filter, startDate, endDate);

        startDate = dateRange.startDate();
        endDate = dateRange.endDate();

        Set<String> filteredHostelIds = new HashSet<>();

        List<KYCUsage> kycUsagesBetweenDates = kycUsageService
                .getAllBetweenDates(startDate, endDate);

        for (KYCUsage kycUsage : kycUsagesBetweenDates) {
            if (kycUsage.getHostelId() != null){
                filteredHostelIds.add(kycUsage.getHostelId());
            }
        }

        if (filteredHostelIds.isEmpty()){
            Map<String, Object> response = new HashMap<>();
            response.put("hostelList", List.of());
            response.put("currentPage", page + 1);
            response.put("pageSize", size);
            response.put("totalItems", 0);
            response.put("totalPages", 0);
            response.put("dateFilters", dateFilters);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        if (isEnabled != null) {
            // enable/disable logic
        }

        Page<HostelV1> pagedHostels = hostelService
                .getKycPagedHostels(name, filteredHostelIds, pageable);

        List<HostelV1> hostels = pagedHostels.getContent();

        Set<String> hostelIds = hostels.stream()
                .map(HostelV1::getHostelId)
                .collect(Collectors.toSet());

        List<Customers> tenants = customersService
                .getCustomersByHostelIds(hostelIds);
        Map<String, List<Customers>> tenantHostelMap = tenants.stream()
                .collect(Collectors.groupingBy(Customers::getHostelId));

        List<KYCUsage> kycUsages = kycUsageService
                .getAllByHostelIds(hostelIds);
        Map<String, KYCUsage> kycUsageHostelMap = kycUsages.stream()
                .collect(Collectors.toMap(KYCUsage::getHostelId,
                        Function.identity(), (a, b) -> a));

        KycHostelResMapper mapper = new KycHostelResMapper(tenantHostelMap, kycUsageHostelMap);

        List<KycHostelRes> responseList = hostels.stream()
                .map(mapper)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("hostelList", responseList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedHostels.getTotalElements());
        response.put("totalPages", pagedHostels.getTotalPages());
        response.put("dateFilters", dateFilters);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getHostelById(String hostelId, int page, int size, String name,
                                           String kycStatus, String dateFilter, Date startDate,
                                           Date endDate) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        HostelV1 hostel = hostelService.getHostelInfo(hostelId);
        if (hostel == null) {
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        name = (name == null || name.isBlank()) ? null : name.trim();
        kycStatus = (kycStatus == null || kycStatus.isBlank()) ? null : kycStatus.trim();

        DateFilterEnum filter;
        try {
            filter = DateFilterEnum.valueOf(dateFilter);
        } catch (Exception e) {
            return new ResponseEntity<>(Utils.DATE_FILTER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<DateFilterRes> dateFilters = Arrays.stream(DateFilterEnum.values())
                .map(i -> new DateFilterRes(i.name(), i.getValue()))
                .toList();

        StartEndDateDto dateRange = Utils.getDateRange(filter, startDate, endDate);

        startDate = dateRange.startDate();
        endDate = dateRange.endDate();

        Set<String> filteredTenantIds = new HashSet<>();

        List<KYCUsage> kycUsagesBetweenDates = kycUsageService
                .getAllBetweenDates(startDate, endDate);

        for (KYCUsage kycUsage : kycUsagesBetweenDates) {
            if (kycUsage.getHostelId() != null){
                filteredTenantIds.add(kycUsage.getLatestRequestTo());
            }
        }

        List<KycStatusResponse> kycStatusFilters = Arrays.stream(KycStatus.values())
                .map(i -> new KycStatusResponse(i.name(), i.getStatus()))
                .toList();

        if (filteredTenantIds.isEmpty()){
            Map<String, Object> response = new HashMap<>();
            response.put("hostel", List.of());
            response.put("currentPage", page + 1);
            response.put("pageSize", size);
            response.put("totalItems", 0);
            response.put("totalPages", 0);
            response.put("dateFilters", dateFilters);
            response.put("kycStatus", kycStatusFilters);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Page<Customers> pagedTenants = customersService
                .getCustomersByHostelIdNameKycStatus(hostelId, name, kycStatus,
                        filteredTenantIds, pageable);

        List<Customers> tenants = pagedTenants.getContent();

        Set<String> tenantIds = tenants.stream()
                .map(Customers::getCustomerId)
                .collect(Collectors.toSet());

        BillingRules billingRule = billingRulesService.getCurrentMonthTemplate(hostelId);
        if (billingRule == null) {
            return new ResponseEntity<>(Utils.BILLING_RULE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<Customers> allTenants = customersService.findCustomersByHostelId(hostelId);

        KycTenantResMapper mapper = new KycTenantResMapper(tenants, billingRule,
                billingRulesService, allTenants);

        KycTenantRes kycTenantRes = mapper.apply(hostel);

        Map<String, Object> response = new HashMap<>();
        response.put("hostel", kycTenantRes);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedTenants.getTotalElements());
        response.put("totalPages", pagedTenants.getTotalPages());
        response.put("dateFilters", dateFilters);
        response.put("kycStatus", kycStatusFilters);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> sendReminder(String customerId) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Customers tenant = customersService.getCustomerInformation(customerId);
        if (tenant == null) {
            return new ResponseEntity<>(Utils.NO_CUSTOMER_FOUND, HttpStatus.BAD_REQUEST);
        }

        HostelV1 hostel = hostelService.getHostelInfo(tenant.getHostelId());
        if (hostel == null) {
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        Users owner = usersService.getOwner(hostel.getParentId());
        if (owner == null){
            return new ResponseEntity<>(Utils.NO_OWNER_FOUND, HttpStatus.BAD_REQUEST);
        }

        KycDetails kycDetails = tenant.getKycDetails();

        KYCUsage kycUsage = kycUsageService.getByHostelId(tenant.getHostelId());

        if (!CustomerStatus.ACTIVE.name().equals(tenant.getCurrentStatus()) &&
            !CustomerStatus.NOTICE.name().equals(tenant.getCurrentStatus()) &&
            !CustomerStatus.CHECK_IN.name().equals(tenant.getCurrentStatus()) &&
            !CustomerStatus.WALKED_IN.name().equals(tenant.getCurrentStatus())) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_ACTIVE, HttpStatus.BAD_REQUEST);
        }

        if (kycDetails != null && kycDetails.getCurrentStatus() != null){
            if (!KycStatus.REQUESTED.name().equals(kycDetails.getCurrentStatus())){
                if (KycStatus.WAITING_FOR_APPROVAL.name().equals(kycDetails.getCurrentStatus())){
                    return new ResponseEntity<>("Kyc status is waiting for approval", HttpStatus.BAD_REQUEST);
                } else if (KycStatus.VERIFIED.name().equals(kycDetails.getCurrentStatus())) {
                    return new ResponseEntity<>("Kyc status is verified already", HttpStatus.BAD_REQUEST);
                } else {
                    return new ResponseEntity<>("Kyc status is not requested", HttpStatus.BAD_REQUEST);
                }
            }
        }

        Date today = new Date();

        String tenantFullName = Utils.getFullName(tenant.getFirstName(), tenant.getLastName());

        DigioInitiateKycRequest initiateKycRequest = new DigioInitiateKycRequest(tenant.getMobile(),
                true, "SMS", tenantFullName,
                "SMARTSTAY-WORKFLOW", true);

        DigioInitiateKycResponse digioInitiateKycResponse = initiateKycApiCall(initiateKycRequest);

        DigioInitiateKycResponse.AccessToken kycAccessToken = digioInitiateKycResponse.accessToken();
        if (kycDetails == null) {
            kycDetails = new KycDetails();
            kycDetails.setCustomers(tenant);
            kycDetails.setCreatedAt(today);
            kycDetails.setCreatedBy(authentication.getName());
        }
        kycDetails.setCurrentStatus(KycStatus.REQUESTED.name());
        kycDetails.setTransactionId(digioInitiateKycResponse.transactionId());
        kycDetails.setTemplateId(digioInitiateKycResponse.templateId());
        kycDetails.setReferenceId(digioInitiateKycResponse.referenceId());

        if (kycAccessToken != null) {
            kycDetails.setEntityId(kycAccessToken.entityId());
            kycDetails.setAccessTokenId(kycAccessToken.id());
            kycDetails.setExpireAt(Utils.stringDateToDate(kycAccessToken.validTill()));
        }

        if (kycUsage == null){
            kycUsage = new KYCUsage();

            kycUsage.setHostelId(tenant.getHostelId());
            kycUsage.setRequestCount(1);
        } else {
            int existingRequestCount = 0;
            if (kycUsage.getRequestCount() != null) {
                existingRequestCount = kycUsage.getRequestCount();
            }
            kycUsage.setRequestCount(existingRequestCount + 1);
        }
        kycUsage.setLatestRequest(today);
        kycUsage.setLatestRequestTo(customerId);

        kycDetailsRepository.save(kycDetails);
        kycUsageService.save(kycUsage);
        customerNotificationsService.sendKycReminderNotification(owner, tenant, kycDetails);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private DigioInitiateKycResponse initiateKycApiCall(DigioInitiateKycRequest initiateKycRequest) {

        String digioInitiateUrl = digioRequestUrl + "with_template";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(digioUserName, digioPassword);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<DigioInitiateKycRequest> request = new HttpEntity<>(initiateKycRequest, headers);

            ResponseEntity<DigioInitiateKycResponse> response = restTemplate.exchange(
                    digioInitiateUrl,
                    HttpMethod.POST,
                    request,
                    DigioInitiateKycResponse.class
            );

            DigioInitiateKycResponse digioInitiateKycResponse = response.getBody();
            if (digioInitiateKycResponse == null) {
                throw new BadRequestException(Utils.RESPONSE_BODY_NOT_FOUND);
            }

            if (response.getStatusCode() == HttpStatus.OK) {
                return digioInitiateKycResponse;
            }  else {
                throw new BadRequestException(Utils.INVALID_REQUEST);
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new BadRequestException(Utils.SERVER_ERROR);
        }
    }
}
