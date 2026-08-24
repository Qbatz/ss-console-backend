package com.smartstay.console.responses.productUpdate;

import java.util.List;

public record ProductUpdateItemResponse(Long productUpdateItemId,
                                        String title,
                                        String description,
                                        String updateType,
                                        String module,
                                        String cta,
                                        String ctaLink,
                                        boolean showCtaButton,
                                        List<String> itemImages,
                                        String createdAtDate,
                                        String createdAtTime,
                                        String updatedAtDate,
                                        String updatedAtTime,
                                        String createdById,
                                        String createdBy,
                                        String updatedById,
                                        String updatedBy,
                                        Long productUpdateId) {
}
