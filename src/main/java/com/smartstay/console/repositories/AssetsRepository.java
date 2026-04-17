package com.smartstay.console.repositories;

import com.smartstay.console.dao.AssetsV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetsRepository extends JpaRepository<AssetsV1, Long> {

    List<AssetsV1> findByHostelId(String hostelId);
}
