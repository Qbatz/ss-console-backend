package com.smartstay.console.repositories;

import com.smartstay.console.dao.HostelV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HostelV1Repositories extends JpaRepository<HostelV1, String> {
}
