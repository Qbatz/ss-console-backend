package com.smartstay.console.repositories;

import com.smartstay.console.dao.ElectricityConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectricityConfigRepository extends JpaRepository<ElectricityConfig, Integer> {
}
