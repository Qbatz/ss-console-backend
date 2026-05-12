package com.smartstay.console.services;

import com.smartstay.console.Mapper.invoiceRedemption.InvoiceRedemptionResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.invoiceRedemption.InvoiceRedemptionSnapshot;
import com.smartstay.console.ennum.*;
import com.smartstay.console.exceptions.BadRequestException;
import com.smartstay.console.payloads.invoiceRedemption.UpdateInvoiceRedemptionPayload;
import com.smartstay.console.repositories.InvoiceRedemptionRepository;
import com.smartstay.console.responses.invoiceRedemption.InvoiceRedemptionRes;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private UsersService usersService;
    @Autowired
    @Lazy
    private InvoiceV1Service invoiceService;
    @Autowired
    private PaymentSummaryService paymentSummaryService;

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
        Set<String> agentIds = new HashSet<>();

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
            if (UserType.OWNER.name().equals(invoiceRedemption.getUserType())){
                if (invoiceRedemption.getUpdatedBy() != null){
                    userIds.add(invoiceRedemption.getUpdatedBy());
                }
            }
            if (UserType.AGENT.name().equals(invoiceRedemption.getUserType())){
                if (invoiceRedemption.getUpdatedBy() != null){
                    agentIds.add(invoiceRedemption.getUpdatedBy());
                }
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

        Map<String, Agent> agentMap = agentService
                .getAgentsByIds(agentIds)
                .stream()
                .collect(Collectors.toMap(
                        Agent::getAgentId,
                        ag -> ag
                ));

        List<InvoiceRedemptionRes> invoiceRedemptionResList = invoiceRedemptions.stream()
                .map(invoiceRedemption -> {

                    HostelV1 hostel = hostelMap.getOrDefault(invoiceRedemption.getHostelId(), null);
                    InvoicesV1 targetInvoice = invoiceMap.getOrDefault(invoiceRedemption.getTargetInvoiceId(), null);
                    InvoicesV1 sourceInvoice = invoiceMap.getOrDefault(invoiceRedemption.getSourceInvoiceId(), null);
                    Users createdByUser = userMap.getOrDefault(invoiceRedemption.getCreatedBy(), null);
                    String updatedBy = null;
                    if (UserType.OWNER.name().equals(invoiceRedemption.getUserType())) {
                        Users updatedByUser = userMap.getOrDefault(invoiceRedemption.getUpdatedBy(), null);
                        if (updatedByUser != null){
                            updatedBy = Utils.getFullName(updatedByUser.getFirstName(), updatedByUser.getLastName());
                        }
                    } else if (UserType.AGENT.name().equals(invoiceRedemption.getUserType())) {
                        Agent updatedByAgent = agentMap.getOrDefault(invoiceRedemption.getUpdatedBy(), null);
                        if (updatedByAgent != null){
                            updatedBy = Utils.getFullName(updatedByAgent.getFirstName(), updatedByAgent.getLastName());
                        }
                    }

                    return new InvoiceRedemptionResMapper(
                            hostel, targetInvoice, sourceInvoice, createdByUser, updatedBy
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

    public List<InvoiceRedemption> getLimitedInvoiceRedemptionsByHostelId(String hostelId, int size){
        Pageable pageable = PageRequest.of(0, size);
        return invoiceRedemptionRepository
                .findAllByHostelIdAndIsActiveTrueOrderByIdDesc(hostelId, pageable)
                .getContent();
    }

    public ResponseEntity<?> getInvoiceRedemptionsByHostelId(String hostelId, int page, int size) {

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

        HostelV1 hostel = hostelService.getHostelInfo(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        Page<InvoiceRedemption> pagedInvoiceRedemptions = invoiceRedemptionRepository
                .findAllByHostelIdAndIsActiveTrueOrderByIdDesc(hostelId, pageable);

        List<InvoiceRedemption> invoiceRedemptions = pagedInvoiceRedemptions.getContent();

        Set<String> invoiceIds = new HashSet<>();
        Set<String> userIds = new HashSet<>();
        Set<String> agentIds = new HashSet<>();

        for (InvoiceRedemption invoiceRedemption : invoiceRedemptions) {
            if (invoiceRedemption.getTargetInvoiceId() != null){
                invoiceIds.add(invoiceRedemption.getTargetInvoiceId());
            }
            if (invoiceRedemption.getSourceInvoiceId() != null){
                invoiceIds.add(invoiceRedemption.getSourceInvoiceId());
            }
            if (invoiceRedemption.getCreatedBy() != null){
                userIds.add(invoiceRedemption.getCreatedBy());
            }
            if (UserType.OWNER.name().equals(invoiceRedemption.getUserType())){
                if (invoiceRedemption.getUpdatedBy() != null){
                    userIds.add(invoiceRedemption.getUpdatedBy());
                }
            }
            if (UserType.AGENT.name().equals(invoiceRedemption.getUserType())){
                if (invoiceRedemption.getUpdatedBy() != null){
                    agentIds.add(invoiceRedemption.getUpdatedBy());
                }
            }
        }

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

        Map<String, Agent> agentMap = agentService
                .getAgentsByIds(agentIds)
                .stream()
                .collect(Collectors.toMap(
                        Agent::getAgentId,
                        ag -> ag
                ));

        List<InvoiceRedemptionRes> invoiceRedemptionResList = invoiceRedemptions.stream()
                .map(invoiceRedemption -> {

                    InvoicesV1 targetInvoice = invoiceMap.getOrDefault(invoiceRedemption.getTargetInvoiceId(), null);
                    InvoicesV1 sourceInvoice = invoiceMap.getOrDefault(invoiceRedemption.getSourceInvoiceId(), null);
                    Users createdByUser = userMap.getOrDefault(invoiceRedemption.getCreatedBy(), null);
                    String updatedBy = null;
                    if (UserType.OWNER.name().equals(invoiceRedemption.getUserType())) {
                        Users updatedByUser = userMap.getOrDefault(invoiceRedemption.getUpdatedBy(), null);
                        if (updatedByUser != null){
                            updatedBy = Utils.getFullName(updatedByUser.getFirstName(), updatedByUser.getLastName());
                        }
                    } else if (UserType.AGENT.name().equals(invoiceRedemption.getUserType())) {
                        Agent updatedByAgent = agentMap.getOrDefault(invoiceRedemption.getUpdatedBy(), null);
                        if (updatedByAgent != null){
                            updatedBy = Utils.getFullName(updatedByAgent.getFirstName(), updatedByAgent.getLastName());
                        }
                    }
                    return new InvoiceRedemptionResMapper(
                            hostel, targetInvoice, sourceInvoice, createdByUser, updatedBy
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

    public ResponseEntity<?> updateInvoiceRedemption(Long invoiceRedemptionId,
                                                     UpdateInvoiceRedemptionPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Invoices.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        InvoiceRedemption invoiceRedemption = invoiceRedemptionRepository
                .findById(invoiceRedemptionId).orElse(null);
        if (invoiceRedemption == null) {
            return new ResponseEntity<>(Utils.INVOICE_REDEMPTION_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        InvoiceRedemptionSnapshot oldInvoiceRedemption = SnapshotUtility.toSnapshot(invoiceRedemption);

        if (invoiceRedemption.getTargetInvoiceId() == null || invoiceRedemption.getSourceInvoiceId() == null) {
            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 sourceInvoice = invoiceService.getInvoiceById(invoiceRedemption.getSourceInvoiceId());
        if (sourceInvoice == null){
            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 targetInvoice = invoiceService.getInvoiceById(invoiceRedemption.getTargetInvoiceId());
        if (targetInvoice == null){
            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        PaymentSummary paymentSummary = paymentSummaryService.getSummaryByCustomerId(targetInvoice.getCustomerId());
        if (paymentSummary == null){
            return new ResponseEntity<>(Utils.PAYMENT_SUMMARY_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (invoiceRedemption.getRedemptionAmount() == null || sourceInvoice.getBalanceAmount() == null ||
                targetInvoice.getPaidAmount() == null || targetInvoice.getTotalAmount() == null||
                paymentSummary.getCreditAmount() == null || paymentSummary.getBalance() == null){
            return new ResponseEntity<>(Utils.INVALID_AMOUNT, HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        double redemptionAmount = invoiceRedemption.getRedemptionAmount();
        double newAmount = payload.amount();

        if (redemptionAmount <= 0) {
            return new ResponseEntity<>(Utils.INVALID_REDEMPTION_AMOUNT, HttpStatus.BAD_REQUEST);
        }

        double differenceAmount = redemptionAmount - newAmount;

        paymentSummary.setCreditAmount(paymentSummary.getCreditAmount() - differenceAmount);
        paymentSummary.setBalance(paymentSummary.getBalance() + differenceAmount);

        double sourceInvoiceNewBalanceAmount = sourceInvoice.getBalanceAmount() + differenceAmount;

        if (sourceInvoiceNewBalanceAmount < 0) {
            return new ResponseEntity<>(Utils.BALANCE_AMOUNT_NOT_ENOUGH, HttpStatus.BAD_REQUEST);
        }

        sourceInvoice.setBalanceAmount(sourceInvoiceNewBalanceAmount);
        sourceInvoice.setUpdatedAt(today);

        double targetInvoiceNewPaidAmount = targetInvoice.getPaidAmount() - differenceAmount;

        if (targetInvoiceNewPaidAmount < 0) {
            return new ResponseEntity<>(Utils.PAID_AMOUNT_GOES_NEGATIVE, HttpStatus.BAD_REQUEST);
        }

        if (targetInvoiceNewPaidAmount > targetInvoice.getTotalAmount()) {
            return new ResponseEntity<>(Utils.PAID_AMOUNT_EXCEEDS_TOTAL_AMOUNT, HttpStatus.BAD_REQUEST);
        }

        targetInvoice.setPaidAmount(targetInvoiceNewPaidAmount);
        targetInvoice.setUpdatedAt(today);

        if (Objects.equals(targetInvoiceNewPaidAmount, targetInvoice.getTotalAmount())){
            targetInvoice.setPaymentStatus(PaymentStatus.PAID.name());
        } else if (targetInvoiceNewPaidAmount <= 0) {
            targetInvoice.setPaymentStatus(PaymentStatus.PENDING.name());
        } else if (targetInvoiceNewPaidAmount > 0 && targetInvoiceNewPaidAmount < targetInvoice.getTotalAmount()) {
            targetInvoice.setPaymentStatus(PaymentStatus.PARTIAL_PAYMENT.name());
        }

        invoiceRedemption.setRedemptionAmount(newAmount);
        invoiceRedemption.setUserType(UserType.AGENT.name());
        invoiceRedemption.setUpdatedBy(authentication.getName());
        invoiceRedemption.setUpdatedAt(today);

        invoiceService.save(sourceInvoice);
        invoiceService.save(targetInvoice);

        paymentSummaryService.save(paymentSummary);
        
        invoiceRedemption = invoiceRedemptionRepository.save(invoiceRedemption);

        InvoiceRedemptionSnapshot newInvoiceRedemption = SnapshotUtility.toSnapshot(invoiceRedemption);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.INVOICE_REDEMPTION,
                String.valueOf(invoiceRedemptionId), oldInvoiceRedemption, newInvoiceRedemption);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteInvoiceRedemption(Long invoiceRedemptionId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Invoices.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        InvoiceRedemption invoiceRedemption = invoiceRedemptionRepository
                .findById(invoiceRedemptionId).orElse(null);
        if (invoiceRedemption == null) {
            return new ResponseEntity<>(Utils.INVOICE_REDEMPTION_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        InvoiceRedemptionSnapshot oldInvoiceRedemption = SnapshotUtility.toSnapshot(invoiceRedemption);

        if (invoiceRedemption.getTargetInvoiceId() == null || invoiceRedemption.getSourceInvoiceId() == null) {
            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 sourceInvoice = invoiceService.getInvoiceById(invoiceRedemption.getSourceInvoiceId());
        if (sourceInvoice == null){
            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 targetInvoice = invoiceService.getInvoiceById(invoiceRedemption.getTargetInvoiceId());
        if (targetInvoice == null){
            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        PaymentSummary paymentSummary = paymentSummaryService.getSummaryByCustomerId(targetInvoice.getCustomerId());
        if (paymentSummary == null){
            return new ResponseEntity<>(Utils.PAYMENT_SUMMARY_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        if (invoiceRedemption.getRedemptionAmount() == null || sourceInvoice.getBalanceAmount() == null ||
                targetInvoice.getPaidAmount() == null || targetInvoice.getTotalAmount() == null ||
                paymentSummary.getCreditAmount() == null || paymentSummary.getBalance() == null){
            return new ResponseEntity<>(Utils.INVALID_AMOUNT, HttpStatus.BAD_REQUEST);
        }

        double redemptionAmount = invoiceRedemption.getRedemptionAmount();

        if (redemptionAmount <= 0) {
            return new ResponseEntity<>(Utils.INVALID_REDEMPTION_AMOUNT, HttpStatus.BAD_REQUEST);
        }

        paymentSummary.setCreditAmount(paymentSummary.getCreditAmount() - redemptionAmount);
        paymentSummary.setBalance(paymentSummary.getBalance() + redemptionAmount);

        double sourceInvoiceNewBalanceAmount = sourceInvoice.getBalanceAmount() + redemptionAmount;

        sourceInvoice.setBalanceAmount(sourceInvoiceNewBalanceAmount);
        sourceInvoice.setUpdatedAt(today);

        double targetInvoiceNewPaidAmount = targetInvoice.getPaidAmount() - redemptionAmount;

        if (targetInvoiceNewPaidAmount < 0) {
            return new ResponseEntity<>(Utils.PAID_AMOUNT_GOES_NEGATIVE, HttpStatus.BAD_REQUEST);
        }

        targetInvoice.setPaidAmount(targetInvoiceNewPaidAmount);
        targetInvoice.setUpdatedAt(today);

        if (Objects.equals(targetInvoiceNewPaidAmount, targetInvoice.getTotalAmount())){
            targetInvoice.setPaymentStatus(PaymentStatus.PAID.name());
        } else if (targetInvoiceNewPaidAmount <= 0) {
            targetInvoice.setPaymentStatus(PaymentStatus.PENDING.name());
        } else if (targetInvoiceNewPaidAmount > 0 && targetInvoiceNewPaidAmount < targetInvoice.getTotalAmount()) {
            targetInvoice.setPaymentStatus(PaymentStatus.PARTIAL_PAYMENT.name());
        }

        invoiceRedemption.setIsActive(false);
        invoiceRedemption.setUserType(UserType.AGENT.name());
        invoiceRedemption.setUpdatedBy(authentication.getName());
        invoiceRedemption.setUpdatedAt(today);

        invoiceService.save(sourceInvoice);
        invoiceService.save(targetInvoice);

        paymentSummaryService.save(paymentSummary);

        invoiceRedemptionRepository.save(invoiceRedemption);

        agentActivitiesService.createAgentActivity(agent, ActivityType.DELETE, Source.INVOICE_REDEMPTION,
                String.valueOf(invoiceRedemptionId), oldInvoiceRedemption, null);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }

    public List<InvoiceRedemption> getInvoiceRedemptionByInvoiceIds(Set<String> invoiceIds) {
        return invoiceRedemptionRepository.findByInvoiceIds(invoiceIds);
    }

    public void deleteInvoiceRedemptions(List<InvoiceRedemption> invoiceRedemptions) {

        Set<String> invoiceIds = new HashSet<>();

        for (InvoiceRedemption invoiceRedemption : invoiceRedemptions) {
            if (invoiceRedemption.getSourceInvoiceId() != null){
                invoiceIds.add(invoiceRedemption.getSourceInvoiceId());
            }
            if (invoiceRedemption.getTargetInvoiceId() != null){
                invoiceIds.add(invoiceRedemption.getTargetInvoiceId());
            }
        }

        List<InvoicesV1> invoices = invoiceService.getInvoicesByIds(invoiceIds);

        Map<String, InvoicesV1> invoiceMap = invoices.stream()
                .collect(Collectors.toMap(InvoicesV1::getInvoiceId, invoice -> invoice));

        Set<String> customerIds = invoices.stream()
                .map(InvoicesV1::getCustomerId)
                .collect(Collectors.toSet());

        List<PaymentSummary> paymentSummaries = paymentSummaryService.getSummaryByCustomerIds(customerIds);

        Map<String, PaymentSummary> paymentSummaryMap = paymentSummaries.stream()
                .collect(Collectors.toMap(PaymentSummary::getCustomerId, payment -> payment,
                        (a, b) -> a));

        Date today = new Date();

        List<InvoicesV1> invoiceList = new ArrayList<>();
        List<PaymentSummary> paymentSummaryList = new ArrayList<>();
        List<InvoiceRedemption> invoiceRedemptionList = new ArrayList<>();

        for (InvoiceRedemption invoiceRedemption : invoiceRedemptions) {

            if (invoiceRedemption == null) {
                throw new BadRequestException(Utils.INVOICE_REDEMPTION_NOT_FOUND);
            }

            if (invoiceRedemption.getTargetInvoiceId() == null || invoiceRedemption.getSourceInvoiceId() == null) {
                throw new BadRequestException(Utils.INVOICE_NOT_FOUND);
            }

            InvoicesV1 sourceInvoice = invoiceMap.getOrDefault(invoiceRedemption.getSourceInvoiceId(), null);
            if (sourceInvoice == null){
                throw new BadRequestException(Utils.INVOICE_NOT_FOUND);
            }

            InvoicesV1 targetInvoice = invoiceMap.getOrDefault(invoiceRedemption.getTargetInvoiceId(),  null);
            if (targetInvoice == null){
                throw new BadRequestException(Utils.INVOICE_NOT_FOUND);
            }

            PaymentSummary paymentSummary = paymentSummaryMap.getOrDefault(targetInvoice.getCustomerId(), null);
            if (paymentSummary == null){
                throw new BadRequestException(Utils.PAYMENT_SUMMARY_NOT_FOUND);
            }

            if (invoiceRedemption.getRedemptionAmount() == null || sourceInvoice.getBalanceAmount() == null ||
                    targetInvoice.getPaidAmount() == null || targetInvoice.getTotalAmount() == null||
                    paymentSummary.getCreditAmount() == null || paymentSummary.getBalance() == null){
                throw new BadRequestException(Utils.INVALID_AMOUNT);
            }

            double redemptionAmount = invoiceRedemption.getRedemptionAmount();

            if (redemptionAmount <= 0) {
                throw new BadRequestException(Utils.INVALID_REDEMPTION_AMOUNT);
            }

            paymentSummary.setCreditAmount(paymentSummary.getCreditAmount() - redemptionAmount);
            paymentSummary.setBalance(paymentSummary.getBalance() + redemptionAmount);

            double sourceInvoiceNewBalanceAmount = sourceInvoice.getBalanceAmount() + redemptionAmount;

            sourceInvoice.setBalanceAmount(sourceInvoiceNewBalanceAmount);
            sourceInvoice.setUpdatedAt(today);

            double targetInvoiceNewPaidAmount = targetInvoice.getPaidAmount() - redemptionAmount;

            if (targetInvoiceNewPaidAmount < 0) {
                throw new BadRequestException(Utils.PAID_AMOUNT_GOES_NEGATIVE);
            }

            targetInvoice.setPaidAmount(targetInvoiceNewPaidAmount);
            targetInvoice.setUpdatedAt(today);

            if (Objects.equals(targetInvoiceNewPaidAmount, targetInvoice.getTotalAmount())){
                targetInvoice.setPaymentStatus(PaymentStatus.PAID.name());
            } else if (targetInvoiceNewPaidAmount <= 0) {
                targetInvoice.setPaymentStatus(PaymentStatus.PENDING.name());
            } else if (targetInvoiceNewPaidAmount > 0 && targetInvoiceNewPaidAmount < targetInvoice.getTotalAmount()) {
                targetInvoice.setPaymentStatus(PaymentStatus.PARTIAL_PAYMENT.name());
            }

            invoiceRedemption.setIsActive(false);
            invoiceRedemption.setUserType(UserType.AGENT.name());
            invoiceRedemption.setUpdatedBy(authentication.getName());
            invoiceRedemption.setUpdatedAt(today);

            invoiceList.add(sourceInvoice);
            invoiceList.add(targetInvoice);

            paymentSummaryList.add(paymentSummary);

            invoiceRedemptionList.add(invoiceRedemption);
        }

        invoiceService.saveAll(invoiceList);
        paymentSummaryService.saveAll(paymentSummaryList);
        invoiceRedemptionRepository.saveAll(invoiceRedemptionList);
    }

    public void deleteAll(List<InvoiceRedemption> invoiceRedemptions) {
        invoiceRedemptionRepository.deleteAll(invoiceRedemptions);
    }
}
