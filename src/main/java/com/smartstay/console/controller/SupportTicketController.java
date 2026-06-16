package com.smartstay.console.controller;

import com.smartstay.console.payloads.supportTicket.SupportTicketAssignPayload;
import com.smartstay.console.payloads.supportTicket.SupportTicketNotesPayload;
import com.smartstay.console.payloads.supportTicket.SupportTicketPayload;
import com.smartstay.console.payloads.supportTicket.SupportTicketStatusPayload;
import com.smartstay.console.services.SupportTicketService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@RequestMapping("/v2/support-ticket")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportTicketService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addSupportTicket(@Valid @RequestPart SupportTicketPayload payload,
                                              @RequestPart(value = "paymentProof", required = false) MultipartFile paymentProof){
        return supportTicketService.addSupportTicket(payload, paymentProof);
    }

    @GetMapping
    public ResponseEntity<?> getAllSupportTickets(@RequestParam(value = "page", defaultValue = "0") int page,
                                                  @RequestParam(value = "size", defaultValue = "10") int size,
                                                  @RequestParam(value = "name", required = false) String name,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate,
                                                  @RequestParam(value = "status", required = false) String status,
                                                  @RequestParam(value = "agentId", required = false) String agentId) {
        return supportTicketService.getAllSupportTickets(page, size, name, startDate, endDate, status, agentId);
    }

    @GetMapping("/{supportTicketId}")
    public ResponseEntity<?> getSupportTicketById(@PathVariable("supportTicketId") Long supportTicketId) {
        return supportTicketService.getSupportTicketById(supportTicketId);
    }

    @PutMapping("/assign/{supportTicketId}")
    public ResponseEntity<?> assignSupportTicket(@PathVariable("supportTicketId") Long supportTicketId,
                                                 @Valid @RequestBody SupportTicketAssignPayload payload) {
        return supportTicketService.assignSupportTicket(supportTicketId, payload);
    }

    @PutMapping("/update-status/{supportTicketId}")
    public ResponseEntity<?> updateSupportTicketStatus(@PathVariable("supportTicketId") Long supportTicketId,
                                                       @Valid @RequestBody SupportTicketStatusPayload payload){
        return supportTicketService.updateSupportTicketStatus(supportTicketId, payload);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return supportTicketService.getStatus();
    }

    @GetMapping("/query-type")
    public ResponseEntity<?> getQueryType() {
        return supportTicketService.getQueryType();
    }

    @GetMapping("/priority")
    public ResponseEntity<?> getPriority() {
        return supportTicketService.getPriority();
    }

    @PostMapping("/notes/{supportTicketId}")
    public ResponseEntity<?> addSupportTicketNotes(@PathVariable("supportTicketId") Long supportTicketId,
                                                   @Valid @RequestBody SupportTicketNotesPayload payload){
        return supportTicketService.addSupportTicketNotes(supportTicketId, payload);
    }

    @GetMapping("/notes/{supportTicketId}")
    public ResponseEntity<?> getSupportTicketNotes(@PathVariable("supportTicketId") Long supportTicketId){
        return supportTicketService.getSupportTicketNotes(supportTicketId);
    }
}
