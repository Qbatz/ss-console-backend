package com.smartstay.console.controller;

import com.smartstay.console.payloads.hostelRelationalAgent.HostelRelationalAgentPayload;
import com.smartstay.console.services.HostelRelationalAgentService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/relational-agent")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class HostelRelationalAgentController {

    @Autowired
    private HostelRelationalAgentService hostelRelationalAgentService;

    @PostMapping("/{parentId}")
    public ResponseEntity<?> assignHostelRelationalAgent(@PathVariable("parentId") String parentId,
                                                         @RequestBody @Valid HostelRelationalAgentPayload payload) {
        return hostelRelationalAgentService.assignHostelRelationalAgent(parentId, payload);
    }

    @GetMapping("/reasons")
    public ResponseEntity<?> getHostelRelationalAgentReasons() {
        return hostelRelationalAgentService.getHostelRelationalAgentReasons();
    }
}
