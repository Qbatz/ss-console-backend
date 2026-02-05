package com.smartstay.console.controller;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.payloads.AddAdmin;
import com.smartstay.console.services.AgentService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/admin")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class  AgentController {
    @Autowired
    private AgentService agentService;

    @PostMapping("")
    public ResponseEntity<?> addAdmin(@Valid @RequestBody AddAdmin addAdmin) {
        return agentService.addAdmin(addAdmin);
    }
}
