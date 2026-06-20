package com.smartstay.console.dto.plans;

import java.util.Date;

public record SmartstayFeaturesSnapshot(Long smartstayFeatureId,
                                        String featureName,
                                        boolean isCommon,
                                        boolean isActive,
                                        Date createdAt,
                                        Date updatedAt) {
}
