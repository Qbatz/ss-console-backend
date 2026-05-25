package com.smartstay.console;

import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.DemoRequestSource;
import com.smartstay.console.ennum.DemoRequestStatus;
import com.smartstay.console.repositories.AgentModulesRepository;
import com.smartstay.console.repositories.DemoRequestRepository;
import com.smartstay.console.services.PlansService;
import com.smartstay.console.utils.Utils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Date;
import java.util.List;
import java.util.Set;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default")})
public class SmartstayConsoleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartstayConsoleApplication.class, args);
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
            AgentModules module15 = repository.findByModuleName("Tenant Summary");
            if (module15 == null) {
                module15 = new AgentModules();
                module15.setModuleName("Tenant Summary");
                repository.save(module15);
            }

            AgentModules module16 = repository.findByModuleName("Reset hostel");
            if (module16 == null) {
                module16 = new AgentModules();
                module16.setModuleName("Reset hostel");
                repository.save(module16);
            }

            AgentModules module17 = repository.findByModuleName("Hostel Activities");
            if (module17 == null) {
                module17 = new AgentModules();
                module17.setModuleName("Hostel Activities");
                repository.save(module17);
            }

            AgentModules module18 = repository.findByModuleName("Expenses");
            if (module18 == null) {
                module18 = new AgentModules();
                module18.setModuleName("Expenses");
                repository.save(module18);
            }

            AgentModules module19 = repository.findByModuleName("Recurring");
            if (module19 == null) {
                module19 = new AgentModules();
                module19.setModuleName("Recurring");
                repository.save(module19);
            }

            AgentModules module20 = repository.findByModuleName("Payments");
            if (module20 == null) {
                module20 = new AgentModules();
                module20.setModuleName("Payments");
                repository.save(module20);
            }
        };
    }

    // IMPORTANT: This migration should be executed ONLY ONCE in production.
    // Remove/comment this bean after successful deployment.
//    @Bean
//    CommandLineRunner updateDemoRequestStatus(DemoRequestRepository demoRequestRepository,
//                                              PlansService plansService) {
//
//        return args -> {
//
//            Set<String> oldStatuses = Set.of(
//                    "REQUESTED", "PENDING", "OPEN",
//                    "ONHOLD", "IN_PROGRESS", "COMPLETED",
//                    "ONBOARDED", "REJECTED", "CLOSED"
//            );
//
//            List<DemoRequest> demoRequests = demoRequestRepository.findAll();
//
//            boolean hasUpdates = false;
//
//            Plans trialPlan = plansService.findTrialPlan();
//
//            String trialPlanCode = trialPlan != null
//                    ? trialPlan.getPlanCode()
//                    : null;
//
//            for (DemoRequest demoRequest : demoRequests) {
//
//                //source migration
//                if (demoRequest.getSource() == null) {
//
//                    demoRequest.setSource(DemoRequestSource.CONSOLE.name());
//
//                    hasUpdates = true;
//                }
//
//                // createdAt migration
//                if (demoRequest.getCreatedAt() == null) {
//
//                    Date createdAt = new Date();
//
//                    if (demoRequest.getBookedFor() != null) {
//
//                        createdAt = demoRequest.getBookedFor();
//
//                    } else if (demoRequest.getRequestedDate() != null) {
//
//                        createdAt = Utils.stringDateTimeToDate(
//                                demoRequest.getRequestedDate(),
//                                demoRequest.getRequestedTime()
//                        );
//                    }
//
//                    demoRequest.setCreatedAt(createdAt);
//
//                    hasUpdates = true;
//                }
//
//                // status migration
//                String demoRequestStatus = demoRequest.getDemoRequestStatus();
//
//                if (demoRequestStatus == null) {
//                    continue;
//                }
//
//                if (!oldStatuses.contains(demoRequestStatus)) {
//                    continue;
//                }
//
//                String status = switch (demoRequestStatus) {
//
//                    case "REQUESTED", "PENDING", "OPEN", "ONHOLD" ->
//                            DemoRequestStatus.NEW.name();
//
//                    case "IN_PROGRESS" ->
//                            DemoRequestStatus.CONTACTED.name();
//
//                    case "COMPLETED" ->
//                            DemoRequestStatus.DEMO_COMPLETED.name();
//
//                    case "ONBOARDED" ->
//                            DemoRequestStatus.TRIAL_STARTED.name();
//
//                    case "REJECTED", "CLOSED" ->
//                            DemoRequestStatus.DROPPED.name();
//
//                    default -> demoRequestStatus;
//                };
//
//                if (DemoRequestStatus.TRIAL_STARTED.name().equals(status)) {
//                    demoRequest.setConvertedToPlanCode(trialPlanCode);
//                }
//
//                demoRequest.setDemoRequestStatus(status);
//
//                hasUpdates = true;
//            }
//
//            if (!hasUpdates) {
//                return;
//            }
//
//            demoRequestRepository.saveAll(demoRequests);
//        };
//    }
}
