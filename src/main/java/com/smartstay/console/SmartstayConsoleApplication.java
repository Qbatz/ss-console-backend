package com.smartstay.console;

import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.dao.RolesPermission;
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

//    @Bean
//    CommandLineRunner updateModules(AgentRolesRepository rolesRepository) {
//        return args -> {
//            AgentRoles agentRoles = new AgentRoles();
//            agentRoles.setRoleName("ADMIN");
//            agentRoles.setIsActive(true);
//            agentRoles.setIsDeleted(false);
//
//			List<RolesPermission> rolesPermissions = new ArrayList<>();
//			for (int i = 1; i <= 13; i++) {
//				RolesPermission perm = new RolesPermission();
//				perm.setModuleId(i);
//				perm.setCanRead(true);
//				perm.setCanWrite(true);
//				perm.setCanUpdate(true);
//				perm.setCanDelete(true);
//				rolesPermissions.add(perm);
//			}
//            agentRoles.setPermissions(rolesPermissions);
//
//            rolesRepository.save(agentRoles);
//
//        };
//    }

}
