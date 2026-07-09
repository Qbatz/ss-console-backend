package com.smartstay.console.responses.users;

public record UserActivitiesResponse(Long activityId,
                                     String description,
                                     String userId,
                                     String userName,
                                     String activityDate,
                                     String activityTime,
                                     String sourceId,
                                     String source,
                                     String activityType,
                                     String platform) {
}
