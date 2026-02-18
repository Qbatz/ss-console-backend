package com.smartstay.console.repositories;

import com.smartstay.console.dao.Floors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FloorsRepository extends JpaRepository<Floors, Integer> {
    int countByHostelId(String hostelId);
}
