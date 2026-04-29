package com.smartstay.console.controller;

import com.smartstay.console.payloads.tableColumns.EditTableColumnsPayload;
import com.smartstay.console.payloads.tableColumns.ResetTableColumnsPayload;
import com.smartstay.console.services.TableColumnsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/table-columns")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class TableColumnsController {

    @Autowired
    private TableColumnsService tableColumnsService;

    @GetMapping
    public ResponseEntity<?> getTableColumns(@RequestParam(value = "page", defaultValue = "0") int page,
                                             @RequestParam(value = "size", defaultValue = "10") int size,
                                             @RequestParam(value = "name", required = false) String name) {
        return tableColumnsService.getTableColumns(page, size, name);
    }

    @PutMapping
    public ResponseEntity<?> updateTableColumns(@RequestBody @Valid EditTableColumnsPayload payload) {
        return tableColumnsService.updateTableColumns(payload);
    }

    @PutMapping("/reset")
    public ResponseEntity<?> resetTableColumns(@RequestBody @Valid ResetTableColumnsPayload payload) {
        return tableColumnsService.resetTableColumns(payload);
    }
}
