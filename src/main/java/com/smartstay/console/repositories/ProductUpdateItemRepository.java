package com.smartstay.console.repositories;

import com.smartstay.console.dao.ProductUpdateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProductUpdateItemRepository extends JpaRepository<ProductUpdateItem, Long> {

    List<ProductUpdateItem> findAllByProductUpdateIdAndIsActiveTrueAndIsDeletedFalse(Long productUpdateId);

    List<ProductUpdateItem> findAllByProductUpdateItemIdInAndIsActiveTrueAndIsDeletedFalse(Set<Long> productUpdateItemIds);
}
