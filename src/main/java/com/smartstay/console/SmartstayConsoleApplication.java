package com.smartstay.console;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.repositories.AgentRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;

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
//			agents.setAgentEmailId("sujithy@s3remotica.com");
//			agents.setRoleId(1l);
//			agents.setCreatedAt(new Date());
//			agents.setIsProfileCompleted(false);
//
//			agentRepository.save(agents);

		};
	}

}
