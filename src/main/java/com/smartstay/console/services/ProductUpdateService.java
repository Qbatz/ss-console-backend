package com.smartstay.console.services;

import com.smartstay.console.Mapper.productUpdate.ProductUpdateListResMapper;
import com.smartstay.console.Mapper.productUpdate.ProductUpdateResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.config.FilesConfig;
import com.smartstay.console.config.S3Service;
import com.smartstay.console.config.UploadFileToS3;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.productUpdate.ProductUpdatePublishStatusCountProjection;
import com.smartstay.console.dto.productUpdate.ProductUpdateSnapshot;
import com.smartstay.console.ennum.*;
import com.smartstay.console.payloads.productUpdate.ProductUpdateEditPayload;
import com.smartstay.console.payloads.productUpdate.ProductUpdateItemPayload;
import com.smartstay.console.payloads.productUpdate.ProductUpdatePayload;
import com.smartstay.console.repositories.ProductUpdateRepository;
import com.smartstay.console.responses.productUpdate.*;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductUpdateService {

    @Autowired
    private ProductUpdateRepository productUpdateRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private ProductUpdateItemService productUpdateItemService;
    @Autowired
    private UploadFileToS3 uploadFileToS3;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private PlansService plansService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private UsersService usersService;

    public ResponseEntity<?> addProductUpdate(ProductUpdatePayload payload,
                                              MultiValueMap<String, MultipartFile> files) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Date today = new Date();

        ProductUpdate productUpdate = new ProductUpdate();

        Date releaseDate = null;
        if (payload.releaseDate() != null){
            releaseDate = Utils.localDateToDate(payload.releaseDate());
        }

        String updateType;
        try {
            updateType = ProductUpdateTypeEnum.valueOf(payload.updateType()).name();
        } catch (Exception e){
            return new ResponseEntity<>("Type is invalid", HttpStatus.BAD_REQUEST);
        }

        String platform;
        try {
            platform = ProductUpdatePlatformEnum.valueOf(payload.platform()).name();
        } catch (Exception e){
            return new ResponseEntity<>("Platform is invalid", HttpStatus.BAD_REQUEST);
        }

        String audience;
        try {
            audience = ProductUpdateAudienceEnum.valueOf(payload.audience()).name();
        } catch (Exception e){
            return new ResponseEntity<>("Audience is invalid", HttpStatus.BAD_REQUEST);
        }

        if (!ProductUpdateAudienceEnum.ALL_OWNERS.name().equals(audience)) {
            if (payload.audienceIds() == null || payload.audienceIds().isEmpty()){
                return new ResponseEntity<>("AudienceIds is required", HttpStatus.BAD_REQUEST);
            }
        }

        String publishStatus;
        try {
            publishStatus = PublishStatusEnum.valueOf(payload.publishStatus()).name();
        } catch (Exception e){
            return new ResponseEntity<>("Publish status is invalid", HttpStatus.BAD_REQUEST);
        }

        Date publishDateTime = null;
        if (PublishStatusEnum.PUBLISHED.name().equals(publishStatus)) {
            publishDateTime = today;
        } else if (PublishStatusEnum.SCHEDULED.name().equals(publishStatus)) {
            if (payload.publishDate() == null || payload.publishTime() == null){
                return new ResponseEntity<>("Publish date time is required", HttpStatus.BAD_REQUEST);
            }
            if (!Utils.checkDateIsFromFutureOrPresent(payload.publishDate(), payload.publishTime())) {
                return new ResponseEntity<>(Utils.DATE_IS_NOT_FROM_FUTURE_OR_PRESENT, HttpStatus.BAD_REQUEST);
            }
            publishDateTime = Utils.localDateTimeToDate(payload.publishDate(), payload.publishTime());
        }

        Date expiryDate = null;
        if (payload.expiryDate() != null){
            expiryDate = Utils.localDateToDate(payload.expiryDate());
        }

        productUpdate.setTitle(payload.title());
        productUpdate.setDescription(payload.description());
        productUpdate.setVersion(payload.version());
        productUpdate.setReleaseDate(releaseDate);
        productUpdate.setUpdateType(updateType);
        productUpdate.setPlatform(platform);
        productUpdate.setAudience(audience);
        productUpdate.setAudienceIds(payload.audienceIds());
        productUpdate.setPublishStatus(publishStatus);
        productUpdate.setPublishDateTime(publishDateTime);
        productUpdate.setExpiryDate(expiryDate);
        productUpdate.setActive(true);
        productUpdate.setDeleted(false);
        productUpdate.setCreatedAt(today);
        productUpdate.setCreatedBy(loggedInAgentId);

        List<ProductUpdateItem> productUpdateItems = new ArrayList<>();

        // validation
        Set<String> clientIds = new HashSet<>();

        for (ProductUpdateItemPayload item : payload.productUpdateItems()) {

            if (!clientIds.add(item.clientId())) {
                return new ResponseEntity<>(
                        "Duplicate clientId: " + item.clientId(),
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        for (String key : files.keySet()) {

            // payload is JSON, not an image
            if ("payload".equals(key)) {
                continue;
            }

            if (!clientIds.contains(key)) {
                return new ResponseEntity<>(
                        "Invalid image clientId: " + key,
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        ResponseEntity<?> imageValidationResponse = validateImages(payload, files);

        if (imageValidationResponse != null) {
            return imageValidationResponse;
        }

        Map<String, ProductUpdateItem> productUpdateItemMap = new HashMap<>();

        for (ProductUpdateItemPayload itemPayload : payload.productUpdateItems()) {

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

            productUpdateItemMap.put(itemPayload.clientId(), productUpdateItem);

            productUpdateItems.add(productUpdateItem);
        }

        List<String> uploadedImageUrls = new ArrayList<>();
        for (ProductUpdateItemPayload itemPayload : payload.productUpdateItems()) {

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

        productUpdate = productUpdateRepository.save(productUpdate);

        for (ProductUpdateItem productUpdateItem : productUpdateItems) {

            productUpdateItem.setProductUpdateId(productUpdate.getProductUpdateId());
        }

        productUpdateItemService.saveAll(productUpdateItems);

        ProductUpdateSnapshot newSnapshot = SnapshotUtility.toSnapshot(productUpdate);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.CREATE, Source.PRODUCT_UPDATE,
                String.valueOf(productUpdate.getProductUpdateId()), null, newSnapshot);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity<?> validateImages(ProductUpdatePayload payload,
                                             MultiValueMap<String, MultipartFile> files) {

        for (ProductUpdateItemPayload itemPayload : payload.productUpdateItems()) {

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

    public ResponseEntity<?> getAudience() {
        List<AudienceResponse> response = Arrays.stream(ProductUpdateAudienceEnum.values())
                .map(audience -> new AudienceResponse(
                        audience.name(), audience.getValue(), audience.getDescription()
                )).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getCta() {
        List<CtaResponse> response = Arrays.stream(ProductUpdateCtaEnum.values())
                .map(cta -> new CtaResponse(
                        cta.name(), cta.getValue()
                )).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getModule() {
        List<ModuleResponse> response = Arrays.stream(ProductUpdateModuleEnum.values())
                .map(module -> new ModuleResponse(
                        module.name(), module.getValue()
                )).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getPlatform() {
        List<PlatformResponse> response = Arrays.stream(ProductUpdatePlatformEnum.values())
                .map(platform -> new PlatformResponse(
                        platform.name(), platform.getValue()
                )).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getType() {
        List<TypeResponse> response = Arrays.stream(ProductUpdateTypeEnum.values())
                .map(type -> new TypeResponse(
                        type.name(), type.getValue()
                )).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getPublishStatus() {
        List<PublishStatusResponse> response = Arrays.stream(PublishStatusEnum.values())
                .filter(publishStatus ->
                        !PublishStatusEnum.ARCHIVED.name().equals(publishStatus.name()))
                .map(publishStatus -> new PublishStatusResponse(
                        publishStatus.name(), publishStatus.getValue(), publishStatus.getDescription()
                )).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getProductUpdate(int page, int size, String name,
                                              String publishStatus, String type) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        name = (name == null || name.isBlank()) ? null : name.trim();
        publishStatus = (publishStatus == null || publishStatus.isBlank()) ? null : publishStatus.trim();
        type = (type == null || type.isBlank()) ? null : type.trim();

        if (publishStatus != null && publishStatus.equals("ALL")){
            publishStatus = null;
        }
        if (type != null && type.equals("ALL")) {
            type = null;
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        Page<ProductUpdate> pagedProductUpdate = productUpdateRepository
                .findPagedProductUpdates(name, publishStatus, type, pageable);

        List<ProductUpdate> productUpdates = pagedProductUpdate.getContent();

        Set<String> agentIds = new HashSet<>();
        for (ProductUpdate productUpdate : productUpdates) {
            if (productUpdate.getCreatedBy() != null){
                agentIds.add(productUpdate.getCreatedBy());
            }
            if (productUpdate.getUpdatedBy() != null){
                agentIds.add(productUpdate.getUpdatedBy());
            }
        }

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId,
                        Function.identity()));

        ProductUpdateListResMapper mapper = new ProductUpdateListResMapper(agentMap);

        List<ProductUpdateListRes> productUpdateListRes = productUpdates.stream()
                .map(mapper)
                .toList();

        List<PublishStatusResponse> publishStatusFilters = Arrays.stream(PublishStatusEnum.values())
                .map(i -> new PublishStatusResponse(
                        i.name(), i.getValue(), i.getDescription()
                )).toList();

        List<TypeResponse> typeFilters = Arrays.stream(ProductUpdateTypeEnum.values())
                .map(i -> new TypeResponse(
                        i.name(), i.getValue()
                )).toList();

        long totalCount = 0;
        long draftCount = 0;
        long scheduledCount = 0;
        long publishedCount = 0;

        ProductUpdatePublishStatusCountProjection countData = productUpdateRepository
                .findPublishStatusCountData();

        if (countData != null) {
            totalCount = countData.getTotalCount();
            draftCount = countData.getDraftCount();
            scheduledCount = countData.getScheduledCount();
            publishedCount = countData.getPublishedCount();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("productUpdateList", productUpdateListRes);
        response.put("totalCount", totalCount);
        response.put("draftCount", draftCount);
        response.put("scheduledCount", scheduledCount);
        response.put("publishedCount", publishedCount);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedProductUpdate.getTotalElements());
        response.put("totalPages", pagedProductUpdate.getTotalPages());
        response.put("publishStatusFilters", publishStatusFilters);
        response.put("typeFilters", typeFilters);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getProductUpdateById(Long productUpdateId) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        ProductUpdate productUpdate = productUpdateRepository
                .findByProductUpdateIdAndIsActiveTrueAndIsDeletedFalse(productUpdateId);
        if (productUpdate == null){
            return new ResponseEntity<>(Utils.PRODUCT_UPDATE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<ProductUpdateItem> productUpdateItems = productUpdateItemService
                .getAllByProductUpdateId(productUpdateId);

        Set<String> agentIds = new HashSet<>();

        if (productUpdate.getCreatedBy() != null){
            agentIds.add(productUpdate.getCreatedBy());
        }
        if (productUpdate.getUpdatedBy() != null){
            agentIds.add(productUpdate.getUpdatedBy());
        }

        for (ProductUpdateItem productUpdateItem : productUpdateItems) {
            if (productUpdateItem.getCreatedBy() != null){
                agentIds.add(productUpdateItem.getCreatedBy());
            }
            if (productUpdateItem.getUpdatedBy() != null){
                agentIds.add(productUpdateItem.getUpdatedBy());
            }
        }

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId,
                        Function.identity()));

        Set<Long> planIds = new HashSet<>();
        Set<String> hostelIds = new HashSet<>();
        Set<String> ownerIds = new HashSet<>();

        if (ProductUpdateAudienceEnum.SELECTED_PLANS.name().equals(productUpdate.getAudience())) {
            planIds = productUpdate.getAudienceIds().stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toSet());
        } else if (ProductUpdateAudienceEnum.SELECTED_HOSTELS.name().equals(productUpdate.getAudience())) {
            hostelIds = new HashSet<>(productUpdate.getAudienceIds());
        } else if (ProductUpdateAudienceEnum.SELECTED_OWNERS.name().equals(productUpdate.getAudience())) {
            ownerIds = new HashSet<>(productUpdate.getAudienceIds());
        }

        List<Plans> plans = plansService.getAllByPlanIds(planIds);
        Map<Long, Plans> plansMap = plans.stream()
                .collect(Collectors.toMap(Plans::getPlanId, Function.identity(),
                        (a, b) -> a));

        List<HostelV1> hostels = hostelService.getHostelsByHostelIds(hostelIds);
        Map<String, HostelV1> hostelMap = hostels.stream()
                .collect(Collectors.toMap(HostelV1::getHostelId, Function.identity(),
                        (a, b) -> a));

        List<Users> owners = usersService.getUsersByIds(ownerIds);
        Map<String, Users> ownerMap = owners.stream()
                .collect(Collectors.toMap(Users::getUserId, Function.identity(),
                        (a, b) -> a));

        ProductUpdateResMapper mapper = new ProductUpdateResMapper(agentMap, productUpdateItems,
                plansMap, hostelMap, ownerMap);

        ProductUpdateResponse response = mapper.apply(productUpdate);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> updateProductUpdate(Long productUpdateId, ProductUpdateEditPayload payload) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        ProductUpdate productUpdate = productUpdateRepository
                .findByProductUpdateIdAndIsActiveTrueAndIsDeletedFalse(productUpdateId);
        if (productUpdate == null){
            return new ResponseEntity<>(Utils.PRODUCT_UPDATE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        ProductUpdateSnapshot oldSnapshot = SnapshotUtility.toSnapshot(productUpdate);

        Date today = new Date();

        if (payload.title() != null && !payload.title().isBlank()){
            productUpdate.setTitle(payload.title());
        }
        if (payload.description() != null && !payload.description().isBlank()){
            productUpdate.setDescription(payload.description());
        }
        if (payload.version() != null && !payload.version().isBlank()){
            productUpdate.setVersion(payload.version());
        }
        if (payload.releaseDate() != null){
            Date releaseDate = Utils.localDateToDate(payload.releaseDate());
            productUpdate.setReleaseDate(releaseDate);
        }
        if (payload.updateType() != null && !payload.updateType().isBlank()){
            String updateType;
            try {
                updateType = ProductUpdateTypeEnum.valueOf(payload.updateType()).name();
            } catch (Exception e){
                return new ResponseEntity<>("Type is invalid", HttpStatus.BAD_REQUEST);
            }
            productUpdate.setUpdateType(updateType);
        }
        if (payload.platform() != null && !payload.platform().isBlank()){
            String platform;
            try {
                platform = ProductUpdatePlatformEnum.valueOf(payload.platform()).name();
            } catch (Exception e){
                return new ResponseEntity<>("Platform is invalid", HttpStatus.BAD_REQUEST);
            }
            productUpdate.setPlatform(platform);
        }
        if (payload.audience() != null && !payload.audience().isBlank()){
            String audience;
            try {
                audience = ProductUpdateAudienceEnum.valueOf(payload.audience()).name();
            } catch (Exception e){
                return new ResponseEntity<>("Audience is invalid", HttpStatus.BAD_REQUEST);
            }
            productUpdate.setAudience(audience);

            if (!ProductUpdateAudienceEnum.ALL_OWNERS.name().equals(audience)) {
                if (payload.audienceIds() == null || payload.audienceIds().isEmpty()){
                    return new ResponseEntity<>("AudienceIds is required", HttpStatus.BAD_REQUEST);
                }

                productUpdate.setAudienceIds(payload.audienceIds());
            }
        }
        if (payload.publishStatus() != null && !payload.publishStatus().isBlank()){
            String publishStatus;
            try {
                publishStatus = PublishStatusEnum.valueOf(payload.publishStatus()).name();
            } catch (Exception e){
                return new ResponseEntity<>("Publish status is invalid", HttpStatus.BAD_REQUEST);
            }
            productUpdate.setPublishStatus(publishStatus);

            Date publishDateTime = null;
            if (PublishStatusEnum.PUBLISHED.name().equals(publishStatus)) {
                publishDateTime = today;
            } else if (PublishStatusEnum.SCHEDULED.name().equals(publishStatus)) {
                if (payload.publishDate() == null || payload.publishTime() == null){
                    return new ResponseEntity<>("Publish date time is required", HttpStatus.BAD_REQUEST);
                }
                if (!Utils.checkDateIsFromFutureOrPresent(payload.publishDate(), payload.publishTime())) {
                    return new ResponseEntity<>(Utils.DATE_IS_NOT_FROM_FUTURE_OR_PRESENT, HttpStatus.BAD_REQUEST);
                }
                publishDateTime = Utils.localDateTimeToDate(payload.publishDate(), payload.publishTime());
            }

            Date expiryDate = null;
            if (payload.expiryDate() != null){
                expiryDate = Utils.localDateToDate(payload.expiryDate());
            }

            productUpdate.setPublishDateTime(publishDateTime);
            productUpdate.setExpiryDate(expiryDate);
        }
        productUpdate.setUpdatedAt(today);
        productUpdate.setUpdatedBy(loggedInAgentId);

        productUpdate = productUpdateRepository.save(productUpdate);

        ProductUpdateSnapshot newSnapshot = SnapshotUtility.toSnapshot(productUpdate);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.UPDATE, Source.PRODUCT_UPDATE,
                String.valueOf(productUpdateId), oldSnapshot, newSnapshot);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> archiveProductUpdate(Long productUpdateId) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        ProductUpdate productUpdate = productUpdateRepository
                .findByProductUpdateIdAndIsActiveTrueAndIsDeletedFalse(productUpdateId);
        if (productUpdate == null){
            return new ResponseEntity<>(Utils.PRODUCT_UPDATE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        ProductUpdateSnapshot oldSnapshot = SnapshotUtility.toSnapshot(productUpdate);

        Date today = new Date();

        productUpdate.setPublishStatus(PublishStatusEnum.ARCHIVED.name());
        productUpdate.setUpdatedAt(today);
        productUpdate.setUpdatedBy(loggedInAgentId);

        productUpdate = productUpdateRepository.save(productUpdate);

        ProductUpdateSnapshot newSnapshot = SnapshotUtility.toSnapshot(productUpdate);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.UPDATE, Source.PRODUCT_UPDATE,
                String.valueOf(productUpdateId), oldSnapshot, newSnapshot);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteProductUpdate(Long productUpdateId) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        ProductUpdate productUpdate = productUpdateRepository
                .findByProductUpdateIdAndIsActiveTrueAndIsDeletedFalse(productUpdateId);
        if (productUpdate == null){
            return new ResponseEntity<>(Utils.PRODUCT_UPDATE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        ProductUpdateSnapshot oldSnapshot = SnapshotUtility.toSnapshot(productUpdate);

        List<ProductUpdateItem> productUpdateItems = productUpdateItemService
                .getAllByProductUpdateId(productUpdateId);

        Date today = new Date();

        productUpdate.setActive(false);
        productUpdate.setDeleted(true);
        productUpdate.setUpdatedAt(today);
        productUpdate.setUpdatedBy(loggedInAgentId);

        for (ProductUpdateItem productUpdateItem : productUpdateItems) {
            productUpdateItem.setActive(false);
            productUpdateItem.setDeleted(true);
            productUpdateItem.setUpdatedAt(today);
            productUpdateItem.setUpdatedBy(loggedInAgentId);
        }

        productUpdateItemService.saveAll(productUpdateItems);

        productUpdate = productUpdateRepository.save(productUpdate);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.DELETE, Source.PRODUCT_UPDATE,
                String.valueOf(productUpdateId), oldSnapshot, null);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }
}
