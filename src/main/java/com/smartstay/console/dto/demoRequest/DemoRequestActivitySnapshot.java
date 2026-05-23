package com.smartstay.console.dto.demoRequest;

import java.util.Date;

public record DemoRequestActivitySnapshot(Long activityId,
                                          String comment,
                                          String description,
                                          String status,
                                          String createdByUserType,
                                          String createdBy,
                                          Date createdAt,
                                          Long requestId) {
}
