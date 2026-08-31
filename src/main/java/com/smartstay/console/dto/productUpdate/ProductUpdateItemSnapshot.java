package com.smartstay.console.dto.productUpdate;

import java.util.Date;
import java.util.List;

public record ProductUpdateItemSnapshot(Long productUpdateItemId,
                                        String title,
                                        String description,
                                        String updateType,
                                        String module,
                                        String cta,
                                        String ctaLink,
                                        boolean showCtaButton,
                                        List<String> itemImages,
                                        boolean isActive,
                                        boolean isDeleted,
                                        Date createdAt,
                                        Date updatedAt,
                                        String createdBy,
                                        String updatedBy,
                                        Long productUpdateId) {
}
