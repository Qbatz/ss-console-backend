package com.smartstay.console.services;

import com.smartstay.console.dao.Credentials;
import com.smartstay.console.repositories.CredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CredentialsService {

    @Autowired
    private CredentialsRepository credentialsRepository;

    public boolean existsByService(String service) {
        return credentialsRepository.existsByService(service);
    }

    public Credentials save(Credentials credentials) {
        return credentialsRepository.save(credentials);
    }

    public Credentials getByService(String service) {
        return credentialsRepository.findByService(service);
    }

    public List<Credentials> getAllCredentials() {
        Set<String> services = new HashSet<>();
        services.add("zoho");
        return credentialsRepository.findAllByServiceNotInOrderByServiceAsc(services);
    }
}
