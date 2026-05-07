package com.smartstay.console.controller;

import com.smartstay.console.services.UsersService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/users")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class UsersController {

    @Autowired
    private UsersService usersService;

    @PutMapping("reset-pin/{userId}")
    public ResponseEntity<?> resetPinById(@PathVariable("userId") String userId) {
        return usersService.resetPinById(userId);
    }
}
