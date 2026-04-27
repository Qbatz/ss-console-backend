package com.smartstay.console.dto.demoRequest;

import java.util.Date;

public record DemoRequestCommentsSnapshot(Long id,
                                          String comment,
                                          String createdByUserType,
                                          String createdBy,
                                          Date createdAt,
                                          Long requestId) {
}
