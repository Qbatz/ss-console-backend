package com.smartstay.console.services;

import com.smartstay.console.dao.ProductUpdateItem;
import com.smartstay.console.repositories.ProductUpdateItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductUpdateItemService {

    @Autowired
    private ProductUpdateItemRepository productUpdateItemRepository;

    public void saveAll(List<ProductUpdateItem> productUpdateItems) {
        productUpdateItemRepository.saveAll(productUpdateItems);
    }

    public List<ProductUpdateItem> getAllByProductUpdateId(Long productUpdateId) {
        return productUpdateItemRepository
                .findAllByProductUpdateIdAndIsActiveTrueAndIsDeletedFalse(productUpdateId);
    }
}
