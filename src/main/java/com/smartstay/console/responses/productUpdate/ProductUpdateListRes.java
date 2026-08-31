package com.smartstay.console.responses.productUpdate;

public record ProductUpdateListRes(Long productUpdateId,
                                   String title,
                                   String description,
                                   String version,
                                   String releaseDate,
                                   String updateType,
                                   String platform,
                                   String publishDate,
                                   String publishTime,
                                   String expiryDate,
                                   String audience,
                                   String publishStatus,
                                   boolean canArchive,
                                   String createdAtDate,
                                   String createdAtTime,
                                   String updatedAtDate,
                                   String updatedAtTime,
                                   String createdById,
                                   String createdBy,
                                   String updatedById,
                                   String updatedBy) {
}
