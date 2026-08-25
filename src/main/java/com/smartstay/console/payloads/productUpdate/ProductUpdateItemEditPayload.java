package com.smartstay.console.payloads.productUpdate;

import java.util.List;

public record ProductUpdateItemEditPayload(Long productUpdateItemId,
                                           String title,
                                           String description,
                                           String updateType,
                                           String module,
                                           String cta,
                                           String ctaLink,
                                           String clientId,
                                           List<String> existingImageUrls) {
}
