package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomersOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CustomersOtpRepository extends JpaRepository<CustomersOtp, Long> {

    CustomersOtp findByXuid(String xuid);

    List<CustomersOtp> findAllByXuidIn(Set<String> xuids);
}
