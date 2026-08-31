package com.smartstay.console.controller;

import com.smartstay.console.payloads.productUpdate.ProductUpdateItemAddPayload;
import com.smartstay.console.payloads.productUpdate.ProductUpdateItemEditPayload;
import com.smartstay.console.payloads.productUpdate.ProductUpdateItemIdPayload;
import com.smartstay.console.services.ProductUpdateItemService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v2/product-update-item")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class ProductUpdateItemController {

    @Autowired
    private ProductUpdateItemService productUpdateItemService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProductUpdateItems(@Valid @RequestPart List<ProductUpdateItemAddPayload> payloads,
                                                   @RequestParam MultiValueMap<String, MultipartFile> files){
        return productUpdateItemService.addProductItems(payloads, files);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductUpdateItems(@Valid @RequestPart List<ProductUpdateItemEditPayload> payloads,
                                                      @RequestParam MultiValueMap<String, MultipartFile> files){
        return productUpdateItemService.updateProductUpdateItems(payloads, files);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteProductUpdateItems(@Valid @RequestBody List<ProductUpdateItemIdPayload> payloads){
        return productUpdateItemService.deleteProductUpdateItems(payloads);
    }
}
