package com.smartstay.console.responses.productUpdate;

import java.util.List;

public record ProductUpdateResponse(Long productUpdateId,
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
                                    List<String> audienceIds,
                                    AudienceResponseWrapper audiences,
                                    String publishStatus,
                                    String createdAtDate,
                                    String createdAtTime,
                                    String updatedAtDate,
                                    String updatedAtTime,
                                    String createdById,
                                    String createdBy,
                                    String updatedById,
                                    String updatedBy,
                                    List<ProductUpdateItemResponse> productUpdateItems) {
}
