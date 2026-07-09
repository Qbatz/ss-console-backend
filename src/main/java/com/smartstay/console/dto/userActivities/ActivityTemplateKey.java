package com.smartstay.console.dto.userActivities;

import com.smartstay.console.ennum.ActivitySource;
import com.smartstay.console.ennum.ActivitySourceType;

public record ActivityTemplateKey(ActivitySource source,
                                  ActivitySourceType type) {
}
