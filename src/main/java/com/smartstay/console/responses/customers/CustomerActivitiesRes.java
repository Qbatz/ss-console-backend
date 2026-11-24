package com.smartstay.console.responses.customers;

import java.util.List;

public record CustomerActivitiesRes(Long activityId,
                                    String customerId,
                                    String customerName,
                                    String hostelId,
                                    String hostelName,
                                    String activityType,
                                    String description,
                                    String source,
                                    String sourceId,
                                    String userType,
                                    String createdAtDate,
                                    String createdAtTime,
                                    String tenantPlatform,
                                    List<String> sourceIds) {
}
