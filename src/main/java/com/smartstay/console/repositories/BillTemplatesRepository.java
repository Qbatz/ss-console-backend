package com.smartstay.console.repositories;

import com.smartstay.console.dao.BillTemplates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillTemplatesRepository extends JpaRepository<BillTemplates, Integer> {
    BillTemplates getByHostelId(String hostelId);
}