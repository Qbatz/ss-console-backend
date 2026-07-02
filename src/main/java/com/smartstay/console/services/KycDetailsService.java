package com.smartstay.console.services;

import com.smartstay.console.Mapper.kycDetails.KycDetailsResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.KycDetails;
import com.smartstay.console.ennum.KycStatus;
import com.smartstay.console.repositories.KycDetailsRepository;
import com.smartstay.console.responses.kycDetails.KycDetailsRes;
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

@Service
public class KycDetailsService {

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
}
