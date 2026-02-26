package com.smartstay.console.controller;

import com.smartstay.console.payloads.AddAdmin;
import com.smartstay.console.payloads.agent.AddMockAgent;
import com.smartstay.console.services.AgentService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/admin")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class  AgentController {
    @Autowired
    private AgentService agentService;

    @PostMapping("")
    public ResponseEntity<?> addAdmin(@Valid @RequestBody AddAdmin addAdmin) {
        return agentService.addAdmin(addAdmin);
    }

    @GetMapping("/")
    public ResponseEntity<?> getAdminDetails() {
        return agentService.getAgentDetails();
    }

    //Api to create mock agents for testing
//    @PostMapping("/add-mock-agent")
//    public ResponseEntity<?> addMockAgent(@Valid @RequestBody AddMockAgent addMockAgent) {
//        return agentService.addMockAgent(addMockAgent);
//    }

    @GetMapping("/get-all-agents")
    public ResponseEntity<?> getAllAgents(){
        return agentService.getAllAgents();
    }

    @PutMapping("/deactivate-agent/{agentId}")
    public ResponseEntity<?> deactivateAgent(@PathVariable String agentId){
        return agentService.deactivateAgent(agentId);
    }
}
