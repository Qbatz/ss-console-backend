package com.smartstay.console;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.AgentModules;
import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.dao.RolesPermission;
import com.smartstay.console.repositories.AgentModulesRepository;
import com.smartstay.console.repositories.AgentRepository;
import com.smartstay.console.repositories.AgentRolesRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default")})
public class SmartstayConsoleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartstayConsoleApplication.class, args);
    }

    @Bean
    CommandLineRunner addUserToPortal(AgentRepository agentRepository) {
        return args -> {
//			Agent agents = new Agent();
//			agents.setIsActive(true);
//			agents.setAgentEmailId("robin.isac@s3remotica.com");
//			agents.setRoleId(1l);
//			agents.setCreatedAt(new Date());
//			agents.setIsProfileCompleted(false);
//
//			agentRepository.save(agents);

        };
    }

    @Bean
    CommandLineRunner updateModules(AgentRolesRepository rolesRepository) {
        return args -> {

            AgentRoles agentRoles = rolesRepository.findByRoleName("CONSOLE-ADMIN-LEVEL-1");
            if (agentRoles == null) {
                agentRoles = new AgentRoles();
                agentRoles.setRoleName("CONSOLE-ADMIN-LEVEL-1");
                agentRoles.setIsActive(true);
                agentRoles.setIsEditable(false);
                agentRoles.setIsDeleted(false);

                List<RolesPermission> rolesPermissions = new ArrayList<>();
                for (int i = 1; i <= 13; i++) {
                    RolesPermission perm = new RolesPermission();
                    perm.setModuleId(i);
                    perm.setCanRead(true);
                    perm.setCanWrite(true);
                    perm.setCanUpdate(true);
                    perm.setCanDelete(true);
                    rolesPermissions.add(perm);
                }
                agentRoles.setPermissions(rolesPermissions);

                rolesRepository.save(agentRoles);
            }

            AgentRoles agentRoles2 = rolesRepository.findByRoleName("CONSOLE-ADMIN-READ-ONLY");
            if (agentRoles2 == null) {
                agentRoles2 = new AgentRoles();
                agentRoles2.setRoleName("CONSOLE-ADMIN-READ-ONLY");
                agentRoles2.setIsActive(true);
                agentRoles2.setIsEditable(false);
                agentRoles2.setIsDeleted(false);

                List<RolesPermission> rolesPermissions = new ArrayList<>();
                for (int i = 1; i <= 13; i++) {
                    RolesPermission perm = new RolesPermission();
                    perm.setModuleId(i);
                    perm.setCanRead(true);
                    perm.setCanWrite(false);
                    perm.setCanUpdate(false);
                    perm.setCanDelete(false);
                    rolesPermissions.add(perm);
                }
                agentRoles2.setPermissions(rolesPermissions);

                rolesRepository.save(agentRoles2);
            }

            AgentRoles agentRoles3 = rolesRepository.findByRoleName("CONSOLE-ADMIN-READ-WRITE-ONLY");
            if (agentRoles3 == null) {
                agentRoles3 = new AgentRoles();
                agentRoles3.setRoleName("CONSOLE-ADMIN-READ-WRITE-ONLY");
                agentRoles3.setIsActive(true);
                agentRoles3.setIsEditable(false);
                agentRoles3.setIsDeleted(false);

                List<RolesPermission> rolesPermissions = new ArrayList<>();
                for (int i = 1; i <= 13; i++) {
                    RolesPermission perm = new RolesPermission();
                    perm.setModuleId(i);
                    perm.setCanRead(true);
                    perm.setCanWrite(true);
                    perm.setCanUpdate(false);
                    perm.setCanDelete(false);
                    rolesPermissions.add(perm);
                }
                agentRoles3.setPermissions(rolesPermissions);

                rolesRepository.save(agentRoles3);
            }


        };
    }

    @Bean
    CommandLineRunner addModules(AgentModulesRepository repository) {
        return args -> {

            AgentModules module1 = repository.findByModuleName("Hostels");
            if (module1 == null) {
                module1 = new AgentModules();
                module1.setModuleName("Hostels");
                repository.save(module1);
            }
            AgentModules module2 = repository.findByModuleName("Tenants");
            if (module2 == null) {
                module2 = new AgentModules();
                module2.setModuleName("Tenants");
                repository.save(module2);
            }

            AgentModules module3 = repository.findByModuleName("Subscriptions");
            if (module3 == null) {
                module3 = new AgentModules();
                module3.setModuleName("Subscriptions");
                repository.save(module3);
            }
            AgentModules module4 = repository.findByModuleName("Plans");
            if (module4 == null) {
                module4 = new AgentModules();
                module4.setModuleName("Plans");
                repository.save(module4);
            }

            AgentModules module5 = repository.findByModuleName("Invoices");
            if (module5 == null) {
                module5 = new AgentModules();
                module5.setModuleName("Invoices");
                repository.save(module5);
            }

            AgentModules module6 = repository.findByModuleName("Admin");
            if (module6 == null) {
                module6 = new AgentModules();
                module6.setModuleName("Admin");
                repository.save(module6);
            }
            AgentModules module7 = repository.findByModuleName("Assets");
            if (module7 == null) {
                module7 = new AgentModules();
                module7.setModuleName("Assets");
                repository.save(module7);
            }
            AgentModules module8 = repository.findByModuleName("Updates");
            if (module8 == null) {
                module8 = new AgentModules();
                module8.setModuleName("Updates");
                repository.save(module8);
            }

            AgentModules module9 = repository.findByModuleName("Agreements");
            if (module9 == null) {
                module9 = new AgentModules();
                module9.setModuleName("Agreements");
                repository.save(module9);
            }
            AgentModules module10 = repository.findByModuleName("Amenities");
            if (module10 == null) {
                module10 = new AgentModules();
                module10.setModuleName("Amenities");
                repository.save(module10);
            }
            AgentModules module11 = repository.findByModuleName("Hostel Transactions");
            if (module11 == null) {
                module11 = new AgentModules();
                module11.setModuleName("Hostel Transactions");
                repository.save(module11);
            }

            AgentModules module12 = repository.findByModuleName("Cities");
            if (module12 == null) {
                module12 = new AgentModules();
                module12.setModuleName("Cities");
                repository.save(module12);
            }
            AgentModules module13 = repository.findByModuleName("States");
            if (module13 == null) {
                module13 = new AgentModules();
                module13.setModuleName("States");
                repository.save(module13);
            }
            AgentModules module14 = repository.findByModuleName("Owners");
            if (module14 == null) {
                module14 = new AgentModules();
                module14.setModuleName("Owners");
                repository.save(module14);
            }
        };
    }

}
