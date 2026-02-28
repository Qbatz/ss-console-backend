package com.smartstay.console.controller;

import com.smartstay.console.payloads.subscription.Subscription;
import com.smartstay.console.services.SubscriptionService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/subscription")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class SubscriptionController {
    @Autowired
    private SubscriptionService subscriptionService;
    @PostMapping("/{hostelId}")
    public ResponseEntity<?> subscribeHostel(@PathVariable("hostelId") String hostelId, @RequestBody Subscription subscription) {
        return subscriptionService.subscribeHostel(hostelId, subscription);
    }

    @GetMapping("")
    public ResponseEntity<?> getSubscriptions(@RequestParam(value = "page", defaultValue = "0") int page,
                                              @RequestParam(value = "size", defaultValue = "10") int size,
                                              @RequestParam(value = "hostelName", required = false) String hostelName){
        return subscriptionService.getSubscriptions(page, size, hostelName);
    }
}
