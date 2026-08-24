package com.smartstay.console.Mapper.productUpdate;

import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.ProductUpdateAudienceEnum;
import com.smartstay.console.ennum.PublishStatusEnum;
import com.smartstay.console.responses.productUpdate.*;
import com.smartstay.console.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ProductUpdateResMapper implements Function<ProductUpdate, ProductUpdateResponse> {

    Map<String, Agent> agentMap;
    List<ProductUpdateItem> productUpdateItems;
    Map<Long, Plans> plansMap;
    Map<String, HostelV1> hostelMap;
    Map<String, Users> ownerMap;

    public ProductUpdateResMapper(Map<String, Agent> agentMap,
                                  List<ProductUpdateItem> productUpdateItems,
                                  Map<Long, Plans> plansMap,
                                  Map<String, HostelV1> hostelMap,
                                  Map<String, Users> ownerMap) {
        this.agentMap = agentMap;
        this.productUpdateItems = productUpdateItems;
        this.plansMap = plansMap;
        this.hostelMap = hostelMap;
        this.ownerMap = ownerMap;
    }

    @Override
    public ProductUpdateResponse apply(ProductUpdate productUpdate) {

        String releaseDate = null;
        if (productUpdate.getReleaseDate() != null){
            releaseDate = Utils.dateToString(productUpdate.getReleaseDate());
        }

        String publishDate = null;
        String publishTime = null;
        if (productUpdate.getPublishDateTime() != null){
            publishDate = Utils.dateToString(productUpdate.getPublishDateTime());
            publishTime = Utils.dateToTime(productUpdate.getPublishDateTime());
        }

        String expiryDate = null;
        if (productUpdate.getExpiryDate() != null){
            expiryDate = Utils.dateToString(productUpdate.getExpiryDate());
        }

        String createdAtDate = null;
        String createdAtTime = null;
        if (productUpdate.getCreatedAt() != null){
            createdAtDate = Utils.dateToString(productUpdate.getCreatedAt());
            createdAtTime = Utils.dateToTime(productUpdate.getCreatedAt());
        }

        String updatedAtDate = null;
        String updatedAtTime = null;
        if (productUpdate.getUpdatedAt() != null){
            updatedAtDate = Utils.dateToString(productUpdate.getUpdatedAt());
            updatedAtTime = Utils.dateToTime(productUpdate.getUpdatedAt());
        }

        String createdBy = null;
        String updatedBy = null;
        if (agentMap != null) {
            if (productUpdate.getCreatedBy() != null){
                Agent createdByAgent = agentMap.getOrDefault(productUpdate.getCreatedBy(), null);
                if (createdByAgent != null){
                    createdBy = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
                }
            }
            if (productUpdate.getUpdatedBy() != null){
                Agent updatedByAgent = agentMap.getOrDefault(productUpdate.getUpdatedBy(), null);
                if (updatedByAgent != null){
                    updatedBy = Utils.getFullName(updatedByAgent.getFirstName(), updatedByAgent.getLastName());
                }
            }
        }

        List<PlanAudienceRes> planAudiences = new ArrayList<>();
        List<HostelAudienceRes> hostelAudiences = new ArrayList<>();
        List<OwnerAudienceRes> ownerAudiences = new ArrayList<>();
        boolean isPlanAudience = false;
        boolean isHostelAudience = false;
        boolean isOwnerAudience = false;
        if (ProductUpdateAudienceEnum.SELECTED_PLANS.name().equals(productUpdate.getAudience())) {
            isPlanAudience = true;
        } else if (ProductUpdateAudienceEnum.SELECTED_HOSTELS.name().equals(productUpdate.getAudience())) {
            isHostelAudience = true;
        } else if (ProductUpdateAudienceEnum.SELECTED_OWNERS.name().equals(productUpdate.getAudience())) {
            isOwnerAudience = true;
        }
        if (productUpdate.getAudienceIds() != null && !productUpdate.getAudienceIds().isEmpty()) {
            for (String audienceId : productUpdate.getAudienceIds()) {

                if (ProductUpdateAudienceEnum.SELECTED_PLANS.name().equals(productUpdate.getAudience())) {

                    Long planId = Long.valueOf(audienceId);

                    if (plansMap != null) {
                        Plans plan = plansMap.getOrDefault(planId, null);

                        if (plan != null) {
                            planAudiences.add(new PlanAudienceRes(planId, plan.getPlanName(),
                                    plan.getPlanCode(), plan.getPlanType()));
                        }
                    }

                } else if (ProductUpdateAudienceEnum.SELECTED_HOSTELS.name().equals(productUpdate.getAudience())) {

                    String hostelId = audienceId;

                    if (hostelMap != null) {
                        HostelV1 hostel = hostelMap.getOrDefault(hostelId, null);

                        if (hostel != null) {
                            hostelAudiences.add(new HostelAudienceRes(hostelId, hostel.getHostelName()));
                        }
                    }

                } else if (ProductUpdateAudienceEnum.SELECTED_OWNERS.name().equals(productUpdate.getAudience())) {

                    String ownerId = audienceId;

                    if (ownerMap != null) {
                        Users owner = ownerMap.getOrDefault(ownerId, null);

                        if (owner != null) {
                            ownerAudiences.add(new OwnerAudienceRes(ownerId, Utils.getFullName(owner.getFirstName(),
                                    owner.getLastName()), owner.getParentId()));
                        }
                    }
                }
            }
        }

        AudienceResponseWrapper audiences = new AudienceResponseWrapper(planAudiences, hostelAudiences, ownerAudiences,
                isPlanAudience, isHostelAudience, isOwnerAudience);

        List<ProductUpdateItemResponse> productUpdateItemResponses = new ArrayList<>();
        if (productUpdateItems != null && !productUpdateItems.isEmpty()) {
            for (ProductUpdateItem productUpdateItem : productUpdateItems) {

                String itemCreatedAtDate = null;
                String itemCreatedAtTime = null;
                if (productUpdateItem.getCreatedAt() != null){
                    itemCreatedAtDate = Utils.dateToString(productUpdateItem.getCreatedAt());
                    itemCreatedAtTime = Utils.dateToTime(productUpdateItem.getCreatedAt());
                }

                String itemUpdatedAtDate = null;
                String itemUpdatedAtTime = null;
                if (productUpdateItem.getUpdatedAt() != null){
                    itemUpdatedAtDate = Utils.dateToString(productUpdateItem.getUpdatedAt());
                    itemUpdatedAtTime = Utils.dateToTime(productUpdateItem.getUpdatedAt());
                }

                String itemCreatedBy = null;
                String itemUpdatedBy = null;
                if (agentMap != null) {
                    if (productUpdateItem.getCreatedBy() != null){
                        Agent itemCreatedByAgent = agentMap.getOrDefault(productUpdateItem.getCreatedBy(), null);
                        if (itemCreatedByAgent != null){
                            itemCreatedBy = Utils.getFullName(itemCreatedByAgent.getFirstName(), itemCreatedByAgent.getLastName());
                        }
                    }
                    if (productUpdateItem.getUpdatedBy() != null){
                        Agent itemUpdatedByAgent = agentMap.getOrDefault(productUpdateItem.getUpdatedBy(), null);
                        if (itemUpdatedByAgent != null){
                            itemUpdatedBy = Utils.getFullName(itemUpdatedByAgent.getFirstName(), itemUpdatedByAgent.getLastName());
                        }
                    }
                }

                ProductUpdateItemResponse productUpdateItemResponse = new ProductUpdateItemResponse(
                        productUpdateItem.getProductUpdateItemId(), productUpdateItem.getTitle(), productUpdateItem.getDescription(),
                        productUpdateItem.getUpdateType(), productUpdateItem.getModule(), productUpdateItem.getCta(),
                        productUpdateItem.getCtaLink(), productUpdateItem.isShowCtaButton(), productUpdateItem.getItemImages(),
                        itemCreatedAtDate, itemCreatedAtTime, itemUpdatedAtDate, itemUpdatedAtTime, productUpdateItem.getCreatedBy(),
                        itemCreatedBy, productUpdateItem.getUpdatedBy(), itemUpdatedBy, productUpdateItem.getProductUpdateId()
                );

                productUpdateItemResponses.add(productUpdateItemResponse);
            }
        }

        boolean canArchive = false;
        if (productUpdate.getPublishStatus() != null){
            if (!PublishStatusEnum.ARCHIVED.name().equals(productUpdate.getPublishStatus())){
                canArchive = true;
            }
        }

        return new ProductUpdateResponse(productUpdate.getProductUpdateId(), productUpdate.getTitle(),
                productUpdate.getDescription(), productUpdate.getVersion(), releaseDate, productUpdate.getUpdateType(),
                productUpdate.getPlatform(), publishDate, publishTime, expiryDate, productUpdate.getAudience(),
                productUpdate.getAudienceIds(), audiences, productUpdate.getPublishStatus(), canArchive,
                createdAtDate, createdAtTime, updatedAtDate, updatedAtTime, productUpdate.getCreatedBy(),
                createdBy, productUpdate.getUpdatedBy(), updatedBy, productUpdateItemResponses);
    }
}
