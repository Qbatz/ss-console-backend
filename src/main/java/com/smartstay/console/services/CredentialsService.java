package com.smartstay.console.services;

import com.smartstay.console.dao.Credentials;
import com.smartstay.console.repositories.CredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return credentialsRepository.findAllByOrderByServiceAsc();
    }
}
