package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.config.FilesConfig;
import com.smartstay.console.config.S3Service;
import com.smartstay.console.config.UploadFileToS3;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.ProductUpdateItem;
import com.smartstay.console.dto.productUpdate.ProductUpdateItemSnapshot;
import com.smartstay.console.dto.productUpdate.ProductUpdateItemSnapshotWrapper;
import com.smartstay.console.ennum.*;
import com.smartstay.console.payloads.productUpdate.*;
import com.smartstay.console.repositories.ProductUpdateItemRepository;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductUpdateItemService {

    @Autowired
    private ProductUpdateItemRepository productUpdateItemRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private UploadFileToS3 uploadFileToS3;
    @Autowired
    private S3Service s3Service;

    public void saveAll(List<ProductUpdateItem> productUpdateItems) {
        productUpdateItemRepository.saveAll(productUpdateItems);
    }

    public List<ProductUpdateItem> getAllByProductUpdateId(Long productUpdateId) {
        return productUpdateItemRepository
                .findAllByProductUpdateIdAndIsActiveTrueAndIsDeletedFalse(productUpdateId);
    }

    public ResponseEntity<?> addProductItems(List<ProductUpdateItemAddPayload> payloads,
                                             MultiValueMap<String, MultipartFile> files) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Date today = new Date();

        List<ProductUpdateItem> productUpdateItems = new ArrayList<>();

        ResponseEntity<?> imageValidationResponse = validateImages(payloads, files);

        if (imageValidationResponse != null) {
            return imageValidationResponse;
        }

        Set<String> clientIds = new HashSet<>();

        for (ProductUpdateItemAddPayload payload : payloads) {

            if (!clientIds.add(payload.clientId())) {
                return new ResponseEntity<>(
                        "Duplicate clientId: " + payload.clientId(),
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        for (String key : files.keySet()) {

            // payload is JSON, not an image
            if ("payloads".equals(key)) {
                continue;
            }

            if (!clientIds.contains(key)) {
                return new ResponseEntity<>(
                        "Invalid image clientId: " + key,
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        Map<String, ProductUpdateItem> productUpdateItemMap = new HashMap<>();

        for (ProductUpdateItemAddPayload itemPayload : payloads) {

            ProductUpdateItem productUpdateItem = new ProductUpdateItem();

            String itemUpdateType;
            try {
                itemUpdateType = ProductUpdateTypeEnum.valueOf(itemPayload.updateType()).name();
            } catch (Exception e){
                return new ResponseEntity<>("Item type is invalid", HttpStatus.BAD_REQUEST);
            }

            String module;
            try {
                module = ProductUpdateModuleEnum.valueOf(itemPayload.module()).name();
            } catch (Exception e){
                return new ResponseEntity<>("Item module is invalid", HttpStatus.BAD_REQUEST);
            }

            String cta;
            try {
                cta = ProductUpdateCtaEnum.valueOf(itemPayload.cta()).name();
            } catch (Exception e){
                return new ResponseEntity<>("Item cta is invalid", HttpStatus.BAD_REQUEST);
            }

            boolean canShowCtaButton = true;
            if (ProductUpdateCtaEnum.NO_CTA.name().equals(cta)) {
                canShowCtaButton = false;
            }

            if (canShowCtaButton) {
                if (itemPayload.ctaLink() == null || itemPayload.ctaLink().isBlank()){
                    return new ResponseEntity<>("Cta link is required", HttpStatus.BAD_REQUEST);
                }
            }

            productUpdateItem.setTitle(itemPayload.title());
            productUpdateItem.setDescription(itemPayload.description());
            productUpdateItem.setUpdateType(itemUpdateType);
            productUpdateItem.setModule(module);
            productUpdateItem.setCta(cta);
            productUpdateItem.setCtaLink(itemPayload.ctaLink());
            productUpdateItem.setShowCtaButton(canShowCtaButton);
            productUpdateItem.setActive(true);
            productUpdateItem.setDeleted(false);
            productUpdateItem.setCreatedAt(today);
            productUpdateItem.setCreatedBy(loggedInAgentId);
            productUpdateItem.setProductUpdateId(itemPayload.productUpdateId());

            productUpdateItemMap.put(itemPayload.clientId(), productUpdateItem);

            productUpdateItems.add(productUpdateItem);
        }

        List<String> uploadedImageUrls = new ArrayList<>();
        for (ProductUpdateItemAddPayload itemPayload : payloads) {

            ProductUpdateItem productUpdateItem = productUpdateItemMap.get(itemPayload.clientId());

            List<MultipartFile> itemImageFiles = files.getOrDefault(itemPayload.clientId(), Collections.emptyList());

            List<String> itemImageUrls = new ArrayList<>();

            if (itemImageFiles.isEmpty()) {
                itemImageUrls = null;
            } else {

                for (MultipartFile itemImageFile : itemImageFiles) {

                    String itemImageUrl;
                    try {
                        itemImageUrl = uploadFileToS3.uploadFileToS3(
                                FilesConfig.convertMultipartToFileNew(itemImageFile), "product-update/items");

                        itemImageUrls.add(itemImageUrl);
                        uploadedImageUrls.add(itemImageUrl);

                    } catch (Exception e) {

                        // delete already uploaded files
                        for (String url : uploadedImageUrls) {
                            try {
                                s3Service.deleteFile(url);
                            } catch (Exception ignored) {
                            }
                        }

                        return new ResponseEntity<>(Utils.FILE_UPLOAD_FAILED, HttpStatus.BAD_REQUEST);
                    }
                }
            }

            productUpdateItem.setItemImages(itemImageUrls);
        }

        productUpdateItems = productUpdateItemRepository.saveAll(productUpdateItems);

        List<ProductUpdateItemSnapshot> newSnapshots = SnapshotUtility
                .toSnapshotList(productUpdateItems, SnapshotUtility::toSnapshot);

        ProductUpdateItemSnapshotWrapper newSnapshotWrapper =  new ProductUpdateItemSnapshotWrapper(newSnapshots);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.SNAPSHOT_CREATE, Source.PRODUCT_UPDATE_ITEM,
                null, null, newSnapshotWrapper);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> updateProductUpdateItems(List<ProductUpdateItemEditPayload> payloads,
                                                      MultiValueMap<String, MultipartFile> files) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Date today = new Date();

        ResponseEntity<?> imageValidationResponse = validateEditImages(payloads, files);

        if (imageValidationResponse != null) {
            return imageValidationResponse;
        }

        Set<String> clientIds = new HashSet<>();

        for (ProductUpdateItemEditPayload payload : payloads) {

            if (!clientIds.add(payload.clientId())) {
                return new ResponseEntity<>(
                        "Duplicate clientId: " + payload.clientId(),
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        for (String key : files.keySet()) {

            // payload is JSON, not an image
            if ("payloads".equals(key)) {
                continue;
            }

            if (!clientIds.contains(key)) {
                return new ResponseEntity<>(
                        "Invalid image clientId: " + key,
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        Set<Long> productUpdateItemIds = payloads.stream()
                .map(ProductUpdateItemEditPayload::productUpdateItemId)
                .collect(Collectors.toSet());

        List<ProductUpdateItem> productUpdateItems = productUpdateItemRepository
                .findAllByProductUpdateItemIdInAndIsActiveTrueAndIsDeletedFalse(productUpdateItemIds);

        List<ProductUpdateItemSnapshot> oldSnapshots = SnapshotUtility
                .toSnapshotList(productUpdateItems, SnapshotUtility::toSnapshot);

        ProductUpdateItemSnapshotWrapper oldSnapshotWrapper =  new ProductUpdateItemSnapshotWrapper(oldSnapshots);

        Map<Long, ProductUpdateItem> productUpdateItemMap = productUpdateItems.stream()
                .collect(Collectors.toMap(ProductUpdateItem::getProductUpdateItemId,
                        Function.identity(), (a, b) -> a));

        Map<String, ProductUpdateItem> updatableProductUpdateItemMap = new HashMap<>();
        List<ProductUpdateItem> updatableProductUpdateItems = new ArrayList<>();

        for (ProductUpdateItemEditPayload itemPayload : payloads) {

            ProductUpdateItem productUpdateItem = productUpdateItemMap
                    .getOrDefault(itemPayload.productUpdateItemId(), null);

            if (productUpdateItem != null) {
                if (itemPayload.title() != null && !itemPayload.title().isBlank()){
                    productUpdateItem.setTitle(itemPayload.title());
                }
                if (itemPayload.description() != null && !itemPayload.description().isBlank()){
                    productUpdateItem.setDescription(itemPayload.description());
                }
                if (itemPayload.updateType() != null && !itemPayload.updateType().isBlank()){
                    String itemUpdateType;
                    try {
                        itemUpdateType = ProductUpdateTypeEnum.valueOf(itemPayload.updateType()).name();
                    } catch (Exception e){
                        return new ResponseEntity<>("Item type is invalid", HttpStatus.BAD_REQUEST);
                    }
                    productUpdateItem.setUpdateType(itemUpdateType);
                }
                if (itemPayload.module() != null && !itemPayload.module().isBlank()){
                    String module;
                    try {
                        module = ProductUpdateModuleEnum.valueOf(itemPayload.module()).name();
                    } catch (Exception e){
                        return new ResponseEntity<>("Item module is invalid", HttpStatus.BAD_REQUEST);
                    }
                    productUpdateItem.setModule(module);
                }
                if (itemPayload.cta() != null && !itemPayload.cta().isBlank()){
                    String cta;
                    try {
                        cta = ProductUpdateCtaEnum.valueOf(itemPayload.cta()).name();
                    } catch (Exception e){
                        return new ResponseEntity<>("Item cta is invalid", HttpStatus.BAD_REQUEST);
                    }
                    productUpdateItem.setCta(cta);

                    boolean canShowCtaButton = true;
                    if (ProductUpdateCtaEnum.NO_CTA.name().equals(cta)) {
                        canShowCtaButton = false;
                    }
                    productUpdateItem.setShowCtaButton(canShowCtaButton);

                    if (canShowCtaButton) {
                        if (itemPayload.ctaLink() == null || itemPayload.ctaLink().isBlank()){
                            return new ResponseEntity<>("Cta link is required", HttpStatus.BAD_REQUEST);
                        }
                    }
                    productUpdateItem.setCtaLink(itemPayload.ctaLink());
                }
                productUpdateItem.setUpdatedAt(today);
                productUpdateItem.setUpdatedBy(loggedInAgentId);

                updatableProductUpdateItemMap.put(itemPayload.clientId(), productUpdateItem);
                updatableProductUpdateItems.add(productUpdateItem);
            }
        }

        List<String> uploadedImageUrls = new ArrayList<>();
        List<String> allImagesToDelete = new ArrayList<>();
        for (ProductUpdateItemEditPayload itemPayload : payloads) {

            ProductUpdateItem productUpdateItem = updatableProductUpdateItemMap.get(itemPayload.clientId());

            List<MultipartFile> itemImageFiles = files.getOrDefault(itemPayload.clientId(), Collections.emptyList());

            List<String> oldImageUrls = productUpdateItem.getItemImages() != null
                    ? productUpdateItem.getItemImages()
                    : new ArrayList<>();

            List<String> existingImageUrls = itemPayload.existingImageUrls() != null
                    ? itemPayload.existingImageUrls().stream()
                        .filter(oldImageUrls::contains)
                        .distinct()
                        .toList()
                    : Collections.emptyList();

            List<String> imagesToDelete = oldImageUrls.stream()
                    .filter(url -> !existingImageUrls.contains(url))
                    .toList();

            List<String> itemImageUrls = new ArrayList<>();

            if (itemImageFiles.isEmpty()) {
                itemImageUrls = null;
            } else {

                for (MultipartFile itemImageFile : itemImageFiles) {

                    String itemImageUrl;
                    try {
                        itemImageUrl = uploadFileToS3.uploadFileToS3(
                                FilesConfig.convertMultipartToFileNew(itemImageFile), "product-update/items");

                        itemImageUrls.add(itemImageUrl);
                        uploadedImageUrls.add(itemImageUrl);

                    } catch (Exception e) {

                        // delete already uploaded files
                        for (String url : uploadedImageUrls) {
                            try {
                                s3Service.deleteFile(url);
                            } catch (Exception ignored) {
                            }
                        }

                        return new ResponseEntity<>(Utils.FILE_UPLOAD_FAILED, HttpStatus.BAD_REQUEST);
                    }
                }
            }

            if (!existingImageUrls.isEmpty()) {
                if (itemImageUrls == null){
                    itemImageUrls = existingImageUrls;
                } else {
                    itemImageUrls.addAll(existingImageUrls);
                }
            }

            allImagesToDelete.addAll(imagesToDelete);

            productUpdateItem.setItemImages(itemImageUrls);
        }

        updatableProductUpdateItems = productUpdateItemRepository
                .saveAll(updatableProductUpdateItems);

        for (String deleteImageUrl : allImagesToDelete){
            try {
                s3Service.deleteFile(deleteImageUrl);
            } catch (Exception ignored) {
            }
        }

        List<ProductUpdateItemSnapshot> newSnapshots = SnapshotUtility
                .toSnapshotList(updatableProductUpdateItems, SnapshotUtility::toSnapshot);

        ProductUpdateItemSnapshotWrapper newSnapshotWrapper =  new ProductUpdateItemSnapshotWrapper(newSnapshots);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.SNAPSHOT_UPDATE, Source.PRODUCT_UPDATE_ITEM,
                null, oldSnapshotWrapper, newSnapshotWrapper);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteProductUpdateItems(List<ProductUpdateItemIdPayload> payloads) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Date today = new Date();

        Set<Long> productUpdateItemIds = payloads.stream()
                .map(ProductUpdateItemIdPayload::productUpdateItemId)
                .collect(Collectors.toSet());

        List<ProductUpdateItem> productUpdateItems = productUpdateItemRepository
                .findAllByProductUpdateItemIdInAndIsActiveTrueAndIsDeletedFalse(productUpdateItemIds);

        List<ProductUpdateItemSnapshot> oldSnapshots = SnapshotUtility
                .toSnapshotList(productUpdateItems, SnapshotUtility::toSnapshot);

        ProductUpdateItemSnapshotWrapper snapshotWrapper =  new ProductUpdateItemSnapshotWrapper(oldSnapshots);

        Map<Long, ProductUpdateItem> productUpdateItemMap = productUpdateItems.stream()
                .collect(Collectors.toMap(ProductUpdateItem::getProductUpdateItemId,
                        Function.identity(), (a, b) -> a));

        List<ProductUpdateItem> updatedProductItems = new ArrayList<>();

        for (ProductUpdateItemIdPayload payload : payloads) {

            ProductUpdateItem productUpdateItem = productUpdateItemMap
                    .getOrDefault(payload.productUpdateItemId(), null);

            if (productUpdateItem != null) {

                productUpdateItem.setActive(false);
                productUpdateItem.setDeleted(true);
                productUpdateItem.setUpdatedAt(today);
                productUpdateItem.setUpdatedBy(loggedInAgentId);

                updatedProductItems.add(productUpdateItem);
            }
        }

        productUpdateItemRepository.saveAll(updatedProductItems);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.SNAPSHOT_DELETE, Source.PRODUCT_UPDATE_ITEM,
                null, snapshotWrapper, null);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }

    private ResponseEntity<?> validateImages(List<ProductUpdateItemAddPayload> payloads,
                                             MultiValueMap<String, MultipartFile> files) {

        for (ProductUpdateItemAddPayload itemPayload : payloads) {

            List<MultipartFile> images =
                    files.getOrDefault(itemPayload.clientId(), Collections.emptyList());

            for (MultipartFile image : images) {

                if (image == null || image.isEmpty()) {
                    return new ResponseEntity<>(
                            "Image cannot be empty",
                            HttpStatus.BAD_REQUEST
                    );
                }

                String contentType = image.getContentType();

                if (contentType == null ||
                        !(contentType.equals("image/jpeg") ||
                                contentType.equals("image/png") ||
                                contentType.equals("image/webp"))) {

                    return new ResponseEntity<>(
                            "Only JPG, PNG and WEBP images are allowed",
                            HttpStatus.BAD_REQUEST
                    );
                }

                if (image.getSize() > 5 * 1024 * 1024) {
                    return new ResponseEntity<>(
                            "Image size cannot exceed 5 MB",
                            HttpStatus.BAD_REQUEST
                    );
                }
            }
        }

        return null;
    }

    private ResponseEntity<?> validateEditImages(List<ProductUpdateItemEditPayload> payloads,
                                                 MultiValueMap<String, MultipartFile> files) {

        for (ProductUpdateItemEditPayload itemPayload : payloads) {

            List<MultipartFile> images =
                    files.getOrDefault(itemPayload.clientId(), Collections.emptyList());

            for (MultipartFile image : images) {

                if (image == null || image.isEmpty()) {
                    return new ResponseEntity<>(
                            "Image cannot be empty",
                            HttpStatus.BAD_REQUEST
                    );
                }

                String contentType = image.getContentType();

                if (contentType == null ||
                        !(contentType.equals("image/jpeg") ||
                                contentType.equals("image/png") ||
                                contentType.equals("image/webp"))) {

                    return new ResponseEntity<>(
                            "Only JPG, PNG and WEBP images are allowed",
                            HttpStatus.BAD_REQUEST
                    );
                }

                if (image.getSize() > 5 * 1024 * 1024) {
                    return new ResponseEntity<>(
                            "Image size cannot exceed 5 MB",
                            HttpStatus.BAD_REQUEST
                    );
                }
            }
        }

        return null;
    }
}
