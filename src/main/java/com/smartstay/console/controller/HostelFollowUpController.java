package com.smartstay.console.controller;

import com.smartstay.console.payloads.hostelFollowUp.HostelFollowUpPayload;
import com.smartstay.console.services.HostelFollowUpService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/hostel-follow-up")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class HostelFollowUpController {

    @Autowired
    private HostelFollowUpService hostelFollowUpService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getHostelFollowUpByHostelId(@PathVariable("hostelId") String hostelId) {
        return hostelFollowUpService.getHostelFollowUpByHostelId(hostelId);
    }

    @PostMapping("/update-status")
    public ResponseEntity<?> updateHostelFollowUpStatus(@RequestBody @Valid HostelFollowUpPayload payload) {
        return hostelFollowUpService.updateHostelFollowUpStatus(payload);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getHostelFollowUpStatus() {
        return hostelFollowUpService.getHostelFollowUpStatus();
    }
}
