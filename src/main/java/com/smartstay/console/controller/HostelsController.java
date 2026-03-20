package com.smartstay.console.controller;

import com.smartstay.console.payloads.hostel.HostelIdPayload;
import com.smartstay.console.payloads.hostel.HostelIdRecDatePayload;
import com.smartstay.console.payloads.hostel.HostelRecDatePayload;
import com.smartstay.console.services.HostelsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v2/hostels")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class HostelsController {

    @Autowired
    private HostelsService hostelsService;

    @GetMapping("")
    public ResponseEntity<?> getAllHostelsNew(@RequestParam(value = "page", defaultValue = "1") int page,
                                           @RequestParam(value = "size", defaultValue = "10") int size,
                                           @RequestParam(value = "hostelName", required = false) String hostelName) {
        return hostelsService.getAllHostelsNew(page, size, hostelName);
    }

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getHostelByHostelId(@PathVariable("hostelId") String hostelId){
        return hostelsService.getHostelByHostelId(hostelId);
    }

    @PostMapping("/hard-reset/{hostelId}")
    public ResponseEntity<?> hardResetHostelTenants(@PathVariable("hostelId") String hostelId,
                                                    @Valid @RequestBody HostelIdPayload hostelIdPayload) {
        return hostelsService.resetHostelTenant(hostelId, hostelIdPayload);
    }

    @DeleteMapping("/expense/{hostelId}")
    public ResponseEntity<?> removeExpenses(@PathVariable("hostelId") String hostelId) {
        return hostelsService.removeExpenses(hostelId);
    }

    @GetMapping("/activities/{hostelId}")
    public ResponseEntity<?> getHostelActivities(@PathVariable String hostelId,
                                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                                 @RequestParam(value = "name", required = false) String name) {
        return hostelsService.getHostelActivities(hostelId, page, size, name);
    }

    @GetMapping("/recurring")
    public ResponseEntity<?> getHostelRecurring(@RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "size", defaultValue = "10") int size,
                                                @RequestParam(value = "hostelName", required = false) String hostelName,
                                                @RequestParam(defaultValue = "TODAY") String filterBy,
                                                @RequestParam(defaultValue = "ALL") String statusFilterBy){
        return hostelsService.getHostelRecurring(page, size, hostelName, filterBy, statusFilterBy);
    }

//    @PostMapping("/recurring/{hostelId}")
//    public ResponseEntity<?> generateRecurring(@PathVariable("hostelId") String hostelId,
//                                               @Valid @RequestBody HostelRecDatePayload hostelRecDatePayload){
//        return hostelsService.generateRecurring(hostelId, hostelRecDatePayload);
//    }

    @PostMapping("/recurring")
    public ResponseEntity<?> generateRecurring(@RequestBody List<HostelIdRecDatePayload> payloads){
        return hostelsService.generateRecurring(payloads);
    }

    @GetMapping("/recurring/{hostelId}")
    public ResponseEntity<?> getRecurringHistoryByHostelId(@PathVariable("hostelId") String hostelId,
                                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "10") int size){
        return hostelsService.getRecurringHistory(hostelId, page, size);
    }
}
