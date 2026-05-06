package com.smartstay.console.repositories;

import com.smartstay.console.dao.UsersConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersConfigRepository extends JpaRepository<UsersConfig, Long> {
}
