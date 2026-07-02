package com.smartstay.console.services;

import com.smartstay.console.Mapper.kycDetails.KycDetailsResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.KycDetails;
import com.smartstay.console.dto.kycDetails.DigioKycResponse;
import com.smartstay.console.ennum.KycStatus;
import com.smartstay.console.repositories.KycDetailsRepository;
import com.smartstay.console.responses.kycDetails.KycDetailsRes;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KycDetailsService {

    @Value("${DIGIO_URL}")
    private String digioUrl;
    @Value("${DIGIO_REQUEST_URL}")
    private String digioRequestUrl;
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
            List<Customers> customers = customersService.getCustomersByName(name);

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

        if (!KycStatus.WAITING_FOR_APPROVAL.name().equalsIgnoreCase(kycDetails.getCurrentStatus())){
            return new ResponseEntity<>("This tenant does not have an approval in waiting", HttpStatus.BAD_REQUEST);
        }

        String digioVerifyUrl = digioUrl + kycDetails.getEntityId() + "/response";
        String digioApprovalRequestUrl = digioRequestUrl + kycDetails.getEntityId() + "/manage_approval";

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
                        kycDetails.setCurrentStatus(KycStatus.VERIFIED.name());
                    } else {
                        HttpHeaders approvalRequestHeaders = new HttpHeaders();
                        approvalRequestHeaders.setBasicAuth(digioUserName, digioPassword);
                        approvalRequestHeaders.setContentType(MediaType.APPLICATION_JSON);

                        Map<String, String> approvalBody = new HashMap<>();
                        approvalBody.put("status", "approved");

                        HttpEntity<Map<String, String>> approvalRequest = new HttpEntity<>(approvalBody, headers);

                        ResponseEntity<DigioKycResponse> approvalResponse = restTemplate.exchange(
                                digioApprovalRequestUrl,
                                HttpMethod.POST,
                                approvalRequest,
                                DigioKycResponse.class
                        );

                        DigioKycResponse approvalDigioKycResponse = approvalResponse.getBody();
                        if (approvalDigioKycResponse == null) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No response body found");
                        }

                        String approvalStatus = approvalDigioKycResponse.status();

                        System.out.println(approvalStatus);
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
}
