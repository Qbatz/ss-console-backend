package com.smartstay.console.repositories;

import com.smartstay.console.dao.Beds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BedsRepository extends JpaRepository<Beds, Integer> {
    int countByHostelId(String hostelId);
}
