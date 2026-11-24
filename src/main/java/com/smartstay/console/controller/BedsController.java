package com.smartstay.console.controller;

import com.smartstay.console.services.BedsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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

    @GetMapping("/hostel-bed-info")
    public ResponseEntity<?> getHostelsWithBedInfo(@RequestParam(value = "page", defaultValue = "1") int page,
                                                   @RequestParam(value = "size", defaultValue = "10") int size,
                                                   @RequestParam(value = "name", required = false) String name){
        return bedsService.getHostelsWithBedInfo(page, size, name);
    }

    @GetMapping("/{bedId}")
    public ResponseEntity<?> getBedDetailsByBedId(@PathVariable("bedId") int bedId,
                                                  @RequestParam(defaultValue = "THIS_MONTH") String dateFilter,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate){
        return bedsService.getBedDetailsByBedId(bedId, dateFilter, startDate, endDate);
    }
}
