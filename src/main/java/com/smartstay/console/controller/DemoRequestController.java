package com.smartstay.console.controller;

import com.smartstay.console.payloads.demoRequest.*;
import com.smartstay.console.services.DemoRequestService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/v2/demo-request")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class DemoRequestController {

    @Autowired
    DemoRequestService demoRequestService;

    @GetMapping
    public ResponseEntity<?> getAllDemoRequests(@RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "size", defaultValue = "10") int size,
                                                @RequestParam(value = "name", required = false) String name,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate,
                                                @RequestParam(value = "status", required = false) String status,
                                                @RequestParam(value = "agentId", required = false) String agentId) {
        return demoRequestService.getAllDemoRequests(page, size, name, startDate, endDate, status, agentId);
    }

    @GetMapping("/{demoRequestId}")
    public ResponseEntity<?> getDemoRequest(@PathVariable("demoRequestId") Long demoRequestId) {
        return demoRequestService.getDemoRequest(demoRequestId);
    }

    @PostMapping
    public ResponseEntity<?> addDemoRequest(@Valid @RequestBody DemoRequestPayload demoRequestPayload) {
        return demoRequestService.addDemoRequest(demoRequestPayload);
    }

    @PutMapping("/assign/{demoRequestId}")
    public ResponseEntity<?> assignDemoRequest(@PathVariable("demoRequestId") Long demoRequestId,
                                               @Valid @RequestBody DemoRequestAssignPayload payload) {
        return demoRequestService.assignDemoRequest(demoRequestId, payload);
    }

    @PutMapping("/drop/{demoRequestId}")
    public ResponseEntity<?> dropDemoRequest(@PathVariable("demoRequestId") Long demoRequestId,
                                             @Valid @RequestBody DemoRequestDroppedPayload payload) {
        return demoRequestService.markAsDropped(demoRequestId, payload);
    }

    @PutMapping("/update-status/{demoRequestId}")
    public ResponseEntity<?> updateDemoRequestStatus(@PathVariable("demoRequestId") Long demoRequestId,
                                                     @Valid @RequestBody DemoRequestStatusPayload demoRequestStatusPayload){
        return demoRequestService.updateDemoRequestStatus(demoRequestId, demoRequestStatusPayload);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getDemoRequestStatus() {
        return demoRequestService.getDemoRequestStatus();
    }

    @GetMapping("/demo-type")
    public ResponseEntity<?> getDemoType() {
        return demoRequestService.getDemoType();
    }

    @GetMapping("/drop-reason")
    public ResponseEntity<?> getDropReason() {
        return demoRequestService.getDropReason();
    }

    @PostMapping("/comment/{demoRequestId}")
    public ResponseEntity<?> addDemoRequestComment(@PathVariable("demoRequestId") Long demoRequestId,
                                                   @RequestBody @Valid DemoRequestCommentPayload payload){
        return demoRequestService.addDemoRequestComment(demoRequestId, payload);
    }

    @GetMapping("/comment/{demoRequestId}")
    public ResponseEntity<?> getDemoRequestComment(@PathVariable("demoRequestId") Long demoRequestId){
        return demoRequestService.getDemoRequestComment(demoRequestId);
    }
}
