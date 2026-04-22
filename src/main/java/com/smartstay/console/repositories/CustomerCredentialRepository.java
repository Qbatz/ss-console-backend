package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CustomerCredentialRepository extends JpaRepository<CustomerCredentials, String> {

    CustomerCredentials findByXuid(String xuid);

    List<CustomerCredentials> findAllByXuidIn(Set<String> xuids);
}
