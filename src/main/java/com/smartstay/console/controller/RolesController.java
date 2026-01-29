package com.smartstay.console.controller;


import com.smartstay.console.payloads.roles.AddRoles;
import com.smartstay.console.payloads.roles.UpdateRoles;
import com.smartstay.console.services.AgentRolesService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/agent-role")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class RolesController {

    @Autowired
    private AgentRolesService agentRolesService;

    @PostMapping("")
    public ResponseEntity<?> addRole(@Valid @RequestBody AddRoles roleDto) {
        return agentRolesService.addRole(roleDto);
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<?> updateRole(@PathVariable("roleId") Long roleId, @RequestBody UpdateRoles updateRoles) {
        return agentRolesService.updateRoleById(roleId, updateRoles);
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<?> deleteRoleById(@PathVariable("roleId") Long roleId) {
        return agentRolesService.deleteRoleById(roleId);
    }

    @GetMapping("")
    public ResponseEntity<?> getAllRoles() {
        return agentRolesService.getAllRoles();
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<?> getRoleById(@PathVariable("roleId") Long roleId) {
        return agentRolesService.getRoleById(roleId);
    }


}
