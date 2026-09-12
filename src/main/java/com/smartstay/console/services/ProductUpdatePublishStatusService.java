package com.smartstay.console.services;

import com.smartstay.console.dao.ProductUpdatePublishStatus;
import com.smartstay.console.repositories.ProductUpdatePublishStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductUpdatePublishStatusService {

    @Autowired
    private ProductUpdatePublishStatusRepository productUpdatePublishStatusRepository;

    public void save(ProductUpdatePublishStatus productUpdatePublishStatus) {
        productUpdatePublishStatusRepository.save(productUpdatePublishStatus);
    }

    public void delete(ProductUpdatePublishStatus productUpdatePublishStatus) {
        productUpdatePublishStatusRepository.delete(productUpdatePublishStatus);
    }

    public ProductUpdatePublishStatus getByProductUpdateId(Long productUpdateId) {
        return productUpdatePublishStatusRepository.findByProductUpdateId(productUpdateId);
    }
}
