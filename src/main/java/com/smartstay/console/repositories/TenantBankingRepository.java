package com.smartstay.console.repositories;

import com.smartstay.console.dao.TenantBanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantBankingRepository extends JpaRepository<TenantBanking, Long> {

    List<TenantBanking> findAllByCustomerIdIn(List<String> customerIds);

    List<TenantBanking> findAllByCustomerId(String customerId);
}
