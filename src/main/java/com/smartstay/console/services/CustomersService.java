package com.smartstay.console.services;

import com.smartstay.console.Mapper.customers.CustomerSumMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.PaymentSummary;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.repositories.CustomersRepository;
import com.smartstay.console.responses.customers.CustomerSummaryResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
}
