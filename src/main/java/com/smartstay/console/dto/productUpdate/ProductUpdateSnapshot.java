package com.smartstay.console.dto.productUpdate;

import java.util.Date;
import java.util.List;

public record ProductUpdateSnapshot(Long productUpdateId,
                                    String title,
                                    String description,
                                    String version,
                                    Date releaseDate,
                                    String updateType,
                                    String platform,
                                    Date publishDateTime,
                                    Date expiryDate,
                                    String audience,
                                    List<String> audienceIds,
                                    String publishStatus,
                                    boolean isActive,
                                    boolean isDeleted,
                                    Date createdAt,
                                    Date updatedAt,
                                    String createdBy,
                                    String updatedBy) {
}
