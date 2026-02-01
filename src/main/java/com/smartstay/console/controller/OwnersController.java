package com.smartstay.console.controller;

import com.smartstay.console.payloads.owners.ResetPassword;
import com.smartstay.console.services.OwnersService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/master")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class OwnersController {

    @Autowired
    OwnersService ownersService;
    @PostMapping("/change-password")
    public ResponseEntity<?> changeAccountPassword(@Valid @RequestBody ResetPassword resetPassword) {
        return ownersService.resetPassword(resetPassword);
    }
}
