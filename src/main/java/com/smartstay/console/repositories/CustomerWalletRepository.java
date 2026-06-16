package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerWalletRepository extends JpaRepository<CustomerWallet, Long> {
}
