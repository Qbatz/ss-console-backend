package com.smartstay.console.repositories;

import com.smartstay.console.dao.ProductUpdatePublishStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUpdatePublishStatusRepository extends JpaRepository<ProductUpdatePublishStatus, Long> {

    ProductUpdatePublishStatus findByProductUpdateId(Long productUpdateId);
}
