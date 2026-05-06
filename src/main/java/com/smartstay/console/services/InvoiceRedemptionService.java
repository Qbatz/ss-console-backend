package com.smartstay.console.services;

import com.smartstay.console.Mapper.invoiceRedemption.InvoiceRedemptionResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.repositories.InvoiceRedemptionRepository;
import com.smartstay.console.responses.invoiceRedemption.InvoiceRedemptionRes;
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
public class InvoiceRedemptionService {

    @Autowired
    private InvoiceRedemptionRepository invoiceRedemptionRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private InvoiceV1Service invoiceService;

    public ResponseEntity<?> getInvoiceRedemption(int page, int size, String name) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Invoices.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);
        name = name == null || name.isBlank() ? null : name.trim();

        Set<String> filteredHostelIds  = null;
        if (name != null) {
            List<HostelV1> hostels = hostelService.getHostelsByHostelName(name);
            filteredHostelIds  = hostels.stream()
                    .map(HostelV1::getHostelId)
                    .collect(Collectors.toSet());

            if (filteredHostelIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("invoiceRedemptionList", Collections.emptyList());
                response.put("currentPage", page + 1);
                response.put("pageSize", size);
                response.put("totalItems", 0);
                response.put("totalPages", 0);

                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<InvoiceRedemption> pagedInvoiceRedemptions = invoiceRedemptionRepository
                .findFilteredInvoiceRedemptions(filteredHostelIds, pageable);

        List<InvoiceRedemption> invoiceRedemptions = pagedInvoiceRedemptions.getContent();

        Set<String> hostelIds = new HashSet<>();
        Set<String> invoiceIds = new HashSet<>();
        Set<String> userIds = new HashSet<>();

        for (InvoiceRedemption invoiceRedemption : invoiceRedemptions) {
            if (invoiceRedemption.getHostelId() != null){
                hostelIds.add(invoiceRedemption.getHostelId());
            }
            if (invoiceRedemption.getTargetInvoiceId() != null){
                invoiceIds.add(invoiceRedemption.getTargetInvoiceId());
            }
            if (invoiceRedemption.getSourceInvoiceId() != null){
                invoiceIds.add(invoiceRedemption.getSourceInvoiceId());
            }
            if (invoiceRedemption.getCreatedBy() != null){
                userIds.add(invoiceRedemption.getCreatedBy());
            }
        }

        Map<String, HostelV1> hostelMap = hostelService
                .getHostelsByHostelIds(hostelIds)
                .stream()
                .collect(Collectors.toMap(
                        HostelV1::getHostelId,
                        hostel -> hostel
                ));

        Map<String, Users> userMap = usersService
                .getUsersByIds(userIds)
                .stream()
                .collect(Collectors.toMap(
                        Users::getUserId,
                        user -> user
                ));

        Map<String, InvoicesV1> invoiceMap = invoiceService
                .getInvoicesByIds(invoiceIds)
                .stream()
                .collect(Collectors.toMap(
                        InvoicesV1::getInvoiceId,
                        invoice -> invoice
                ));

        List<InvoiceRedemptionRes> invoiceRedemptionResList = invoiceRedemptions.stream()
                .map(invoiceRedemption -> {

                    HostelV1 hostel = hostelMap.getOrDefault(invoiceRedemption.getHostelId(), null);
                    InvoicesV1 targetInvoice = invoiceMap.getOrDefault(invoiceRedemption.getTargetInvoiceId(), null);
                    InvoicesV1 sourceInvoice = invoiceMap.getOrDefault(invoiceRedemption.getSourceInvoiceId(), null);
                    Users createdByUser = userMap.getOrDefault(invoiceRedemption.getCreatedBy(), null);

                    return new InvoiceRedemptionResMapper(
                            hostel, targetInvoice, sourceInvoice, createdByUser
                    ).apply(invoiceRedemption);
                }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("invoiceRedemptionList", invoiceRedemptionResList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedInvoiceRedemptions.getTotalElements());
        response.put("totalPages", pagedInvoiceRedemptions.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
