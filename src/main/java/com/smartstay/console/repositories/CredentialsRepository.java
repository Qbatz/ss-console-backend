package com.smartstay.console.repositories;

import com.smartstay.console.dao.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials, String> {

    boolean existsByService(String service);

    Credentials findByService(String service);

    List<Credentials> findAllByServiceNotInOrderByServiceAsc(Set<String> services);
}
