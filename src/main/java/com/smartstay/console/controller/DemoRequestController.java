package com.smartstay.console.controller;

import com.smartstay.console.payloads.agent.AgentIdPayload;
import com.smartstay.console.payloads.demoRequest.DemoRequestCommentPayload;
import com.smartstay.console.payloads.demoRequest.DemoRequestPayload;
import com.smartstay.console.payloads.demoRequest.DemoRequestStatusPayload;
import com.smartstay.console.services.DemoRequestService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/demo-request")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class DemoRequestController {

    @Autowired
    DemoRequestService demoRequestService;

    @GetMapping("")
    public ResponseEntity<?> getAllDemoRequests(@RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "size", defaultValue = "10") int size,
                                                @RequestParam(value = "name", required = false) String name) {
        return demoRequestService.getAllDemoRequests(page, size, name);
    }

    @PutMapping("/assign/{demoRequestId}")
    public ResponseEntity<?> assignDemoRequest(@PathVariable("demoRequestId") Long demoRequestId,
                                               @Valid @RequestBody AgentIdPayload agentIdPayload) {
        return demoRequestService.assignDemoRequest(demoRequestId, agentIdPayload);
    }

    @GetMapping("/{demoRequestId}")
    public ResponseEntity<?> getDemoRequest(@PathVariable("demoRequestId") Long demoRequestId) {
        return demoRequestService.getDemoRequest(demoRequestId);
    }

    @PostMapping("/")
    public ResponseEntity<?> addDemoRequest(@Valid @RequestBody DemoRequestPayload demoRequestPayload) {
        return demoRequestService.addDemoRequest(demoRequestPayload);
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

    @PostMapping("/comment/{demoRequestId}")
    public ResponseEntity<?> addDemoRequestComment(@PathVariable("demoRequestId") Long demoRequestId,
                                                   @RequestBody @Valid DemoRequestCommentPayload payload){
        return demoRequestService.addDemoRequestComment(demoRequestId, payload);
    }
}
