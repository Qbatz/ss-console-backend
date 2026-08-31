package com.smartstay.console.Mapper.productUpdate;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.ProductUpdate;
import com.smartstay.console.ennum.PublishStatusEnum;
import com.smartstay.console.responses.productUpdate.ProductUpdateListRes;
import com.smartstay.console.utils.Utils;

import java.util.Map;
import java.util.function.Function;

public class ProductUpdateListResMapper implements Function<ProductUpdate, ProductUpdateListRes> {

    Map<String, Agent> agentMap;

    public ProductUpdateListResMapper(Map<String, Agent> agentMap) {
        this.agentMap = agentMap;
    }

    @Override
    public ProductUpdateListRes apply(ProductUpdate productUpdate) {

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

        boolean canArchive = false;
        if (productUpdate.getPublishStatus() != null){
            if (!PublishStatusEnum.ARCHIVED.name().equals(productUpdate.getPublishStatus())){
                canArchive = true;
            }
        }

        return new ProductUpdateListRes(productUpdate.getProductUpdateId(), productUpdate.getTitle(),
                productUpdate.getDescription(), productUpdate.getVersion(), releaseDate, productUpdate.getUpdateType(),
                productUpdate.getPlatform(), publishDate, publishTime, expiryDate, productUpdate.getAudience(),
                productUpdate.getPublishStatus(), canArchive, createdAtDate, createdAtTime, updatedAtDate, updatedAtTime,
                productUpdate.getCreatedBy(), createdBy, productUpdate.getUpdatedBy(), updatedBy);
    }
}
