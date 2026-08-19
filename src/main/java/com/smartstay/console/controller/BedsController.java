package com.smartstay.console.controller;

import com.smartstay.console.services.BedsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/beds")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class BedsController {

    @Autowired
    private BedsService bedsService;

    @PutMapping("/update-current-status/{bedId}")
    public ResponseEntity<?> updateCurrentStatus(@PathVariable("bedId") int bedId) {
        return bedsService.updateBedCurrentStatus(bedId);
    }
}
