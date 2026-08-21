package com.smartstay.console.controller;

import com.smartstay.console.payloads.productUpdate.ProductUpdatePayload;
import com.smartstay.console.services.ProductUpdateService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v2/product-update")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class ProductUpdateController {

    @Autowired
    private ProductUpdateService productUpdateService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProductUpdate(@Valid @RequestPart("payload") ProductUpdatePayload payload,
                                              @RequestParam MultiValueMap<String, MultipartFile> files){
        return productUpdateService.addProductUpdate(payload, files);
    }

    @GetMapping("/audience")
    public ResponseEntity<?> getAudience(){
        return productUpdateService.getAudience();
    }

    @GetMapping("/cta")
    public ResponseEntity<?> getCta(){
        return productUpdateService.getCta();
    }

    @GetMapping("/module")
    public ResponseEntity<?> getModule(){
        return productUpdateService.getModule();
    }

    @GetMapping("/platform")
    public ResponseEntity<?> getPlatform(){
        return productUpdateService.getPlatform();
    }

    @GetMapping("/type")
    public ResponseEntity<?> getType(){
        return productUpdateService.getType();
    }

    @GetMapping("/publish-status")
    public ResponseEntity<?> getPublishStatus(){
        return productUpdateService.getPublishStatus();
    }
}
