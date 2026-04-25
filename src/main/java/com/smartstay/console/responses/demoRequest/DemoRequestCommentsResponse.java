package com.smartstay.console.responses.demoRequest;

public record DemoRequestCommentsResponse(Long demoRequestCommentsId,
                                          String comment,
                                          String createdByUserType,
                                          String createdBy,
                                          String createdAtDate,
                                          String createdAtTime) {
}
