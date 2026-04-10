package com.smartstay.console.controller;

import com.smartstay.console.payloads.customers.CustomerIdPayload;
import com.smartstay.console.payloads.hostel.HostelIdPayload;
import com.smartstay.console.services.HostelsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
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
                                              @RequestParam(value = "hostelName", required = false) String hostelName,
                                              @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
                                              @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate) {
        return hostelsService.getAllHostelsNew(page, size, hostelName, startDate, endDate);
    }

    @GetMapping("/export")
    public void exportHostels(@RequestParam(required = false) String hostelName,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate,
                              HttpServletResponse response) throws IOException {
        hostelsService.exportHostels(hostelName, startDate, endDate, response);
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
                                                @RequestParam(defaultValue = "ALL") String statusFilterBy,
                                                @RequestParam(defaultValue = "ALL") String billingModelFilterBy,
                                                @RequestParam(defaultValue = "0") int billingCycleStartDay){
        return hostelsService.getHostelRecurring(page, size, hostelName, filterBy, statusFilterBy,
                billingModelFilterBy, billingCycleStartDay);
    }

    @GetMapping("/recurring/month")
    public ResponseEntity<?> getRecurringMonthData(@RequestParam(value = "month") int month,
                                                   @RequestParam(value = "year") int year){
        return hostelsService.getMonthlyRecurringSummary(month, year);
    }

    @PostMapping("/recurring")
    public ResponseEntity<?> generateRecurring(@RequestBody List<HostelIdPayload> payloads){
        return hostelsService.generateRecurring(payloads);
    }

    @GetMapping("/recurring/{hostelId}")
    public ResponseEntity<?> getRecurringHistoryByHostelId(@PathVariable("hostelId") String hostelId,
                                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "10") int size){
        return hostelsService.getRecurringHistory(hostelId, page, size);
    }

    @GetMapping("/tenant-recurring")
    public ResponseEntity<?> getTenantRecurring(@RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "size", defaultValue = "10") int size,
                                                @RequestParam(value = "name", required = false) String name,
                                                @RequestParam(defaultValue = "TODAY") String filterBy,
                                                @RequestParam(defaultValue = "ALL") String statusFilterBy,
                                                @RequestParam(defaultValue = "ALL") String billingModelFilterBy,
                                                @RequestParam(defaultValue = "0") int billingCycleStartDay,
                                                @RequestParam(defaultValue = "false") boolean isHostelBased){
        return hostelsService.getTenantRecurring(page, size, name, filterBy, statusFilterBy,
                billingModelFilterBy, billingCycleStartDay, isHostelBased);
    }

    @PostMapping("/tenant-recurring")
    public ResponseEntity<?> generateTenantRecurring(@RequestBody List<CustomerIdPayload> payloads){
        return hostelsService.generateTenantRecurring(payloads);
    }

    @GetMapping("/tenant-recurring/{tenantId}")
    public ResponseEntity<?> getTenantRecurringHistoryByHostelId(@PathVariable("tenantId") String tenantId,
                                                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                                                 @RequestParam(value = "size", defaultValue = "10") int size){
        return hostelsService.getTenantRecurringHistory(tenantId, page, size);
    }
}
