package com.smartstay.console.repositories;

import com.smartstay.console.dao.ProductUpdateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUpdateItemRepository extends JpaRepository<ProductUpdateItem, Long> {
}
