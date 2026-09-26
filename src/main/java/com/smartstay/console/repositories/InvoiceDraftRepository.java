package com.smartstay.console.repositories;

import com.smartstay.console.dao.InvoiceDrafts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceDraftRepository extends JpaRepository<InvoiceDrafts, Long> {
}
