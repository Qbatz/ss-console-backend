package com.smartstay.console.repositories;

import com.smartstay.console.dao.ProductUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUpdateRepository extends JpaRepository<ProductUpdate, Long> {
}
