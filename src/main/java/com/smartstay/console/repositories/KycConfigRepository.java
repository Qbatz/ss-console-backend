package com.smartstay.console.repositories;

import com.smartstay.console.dao.KycConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KycConfigRepository extends JpaRepository<KycConfig, Long> {

    KycConfig findByHostelId(String hostelId);
}
