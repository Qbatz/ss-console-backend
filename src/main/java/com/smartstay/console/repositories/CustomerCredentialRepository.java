package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerCredentialRepository extends JpaRepository<CustomerCredentials, String> {

}
