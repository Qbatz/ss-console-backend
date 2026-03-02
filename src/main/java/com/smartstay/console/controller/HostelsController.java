package com.smartstay.console.controller;

import com.smartstay.console.services.HostelsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/hostels")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class HostelsController {
    @Autowired
    private HostelsService hostelsService;
    @GetMapping("")
    public ResponseEntity<?> getAllHostels( @RequestParam(value = "page", defaultValue = "0") int page,
                                            @RequestParam(value = "size", defaultValue = "10") int size,
                                            @RequestParam(value = "hostelName", required = false) String hostelName) {
        return hostelsService.getAllHostels(page, size, hostelName);
    }

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getHostelByHostelId(@PathVariable String hostelId){
        return hostelsService.getHostelByHostelId(hostelId);
    }

    @PostMapping("/hard-reset/{hostelId}")
    public ResponseEntity<?> hardReserHostelTenants(@PathVariable("hostelId") String hostelId) {
        return hostelsService.resetHostelTenats(hostelId);
    }
}
