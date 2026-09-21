package com.smartstay.console.controller;

import com.smartstay.console.services.DataArchiveService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/v2/data-archive")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class DataArchiveController {

    @Autowired
    private DataArchiveService dataArchiveService;

    @PostMapping("/activities/hostel/{hostelId}")
    public ResponseEntity<?> archiveHostelActivities(@PathVariable("hostelId") String hostelId) throws IOException {
        return dataArchiveService.archiveHostelActivities(hostelId);
    }

    @PostMapping("/restore/{archiveId}")
    public ResponseEntity<?> restoreArchive(@PathVariable("archiveId") Long archiveId) {
        return dataArchiveService.restoreArchive(archiveId);
    }

    @GetMapping
    public ResponseEntity<?> getDataArchives(@RequestParam(value = "page", defaultValue = "0") int page,
                                             @RequestParam(value = "size", defaultValue = "10") int size) {
        return dataArchiveService.getDataArchives(page, size);
    }

    @GetMapping("/{archiveId}")
    public ResponseEntity<?> getDataArchiveById(@PathVariable("archiveId") Long archiveId) {
        return dataArchiveService.getDataArchiveById(archiveId);
    }
}
