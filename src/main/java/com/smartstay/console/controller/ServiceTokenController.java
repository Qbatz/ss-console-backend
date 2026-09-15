package com.smartstay.console.controller;

import com.smartstay.console.payloads.serviceToken.GenerateServiceTokenPayload;
import com.smartstay.console.services.ServiceTokenService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/service-token")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class ServiceTokenController {

    @Autowired
    private ServiceTokenService serviceTokenService;

    @GetMapping("/services")
    public ResponseEntity<?> getServices(){
        return serviceTokenService.getServices();
    }

    @GetMapping("/secret")
    public ResponseEntity<?> getSecret(){
        return serviceTokenService.getSecret();
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateServiceToken(@Valid @RequestBody GenerateServiceTokenPayload payload){
        return serviceTokenService.generateServiceToken(payload);
    }

    @PutMapping("/re-generate")
    public ResponseEntity<?> reGenerateServiceToken(@Valid @RequestBody GenerateServiceTokenPayload payload){
        return serviceTokenService.reGenerateServiceToken(payload);
    }

    @PutMapping("/revoke")
    public ResponseEntity<?> revokeServiceToken(@RequestParam(value = "service") String service){
        return serviceTokenService.revokeServiceToken(service);
    }

    @GetMapping
    public ResponseEntity<?> getServiceTokens(@RequestParam(value = "page", defaultValue = "1") int page,
                                              @RequestParam(value = "size", defaultValue = "10") int size,
                                              @RequestParam(value = "name", required = false) String name,
                                              @RequestParam(defaultValue = "ALL") String status){
        return serviceTokenService.getServiceTokens(page, size, name, status);
    }

    @GetMapping("/service")
    public ResponseEntity<?> getServiceTokenByService(@RequestParam(value = "service") String service){
        return serviceTokenService.getServiceTokenByService(service);
    }
}
