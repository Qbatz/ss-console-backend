package com.smartstay.console.controller;

import com.smartstay.console.payloads.subscription.Subscription;
import com.smartstay.console.services.SubscriptionService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v2/subscription")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping(value = "/{hostelId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subscribeHostel(@PathVariable("hostelId") String hostelId,
                                             @RequestPart @Valid Subscription subscription,
                                             @RequestPart(value = "paymentProof", required = false) MultipartFile paymentProof) {
        return subscriptionService.subscribeHostel(hostelId, subscription, paymentProof);
    }

    @GetMapping("")
    public ResponseEntity<?> getSubscriptions(@RequestParam(value = "page", defaultValue = "0") int page,
                                              @RequestParam(value = "size", defaultValue = "10") int size,
                                              @RequestParam(value = "hostelName", required = false) String hostelName){
        return subscriptionService.getSubscriptions(page, size, hostelName);
    }
}
