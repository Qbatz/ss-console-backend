package com.smartstay.console.controller;

import com.smartstay.console.services.OrderHistoryService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/v2/order-history")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class OrderHistoryController {

    @Autowired
    private OrderHistoryService orderHistoryService;

    @GetMapping
    public ResponseEntity<?> getOrderHistory(@RequestParam(value = "page", defaultValue = "0") int page,
                                             @RequestParam(value = "size", defaultValue = "10") int size,
                                             @RequestParam(value = "name", required = false) String name,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate) {
        return orderHistoryService.getOrderHistory(page, size, name, startDate, endDate);
    }
}
