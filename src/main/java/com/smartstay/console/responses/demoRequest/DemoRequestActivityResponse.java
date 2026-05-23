package com.smartstay.console.responses.demoRequest;

public record DemoRequestActivityResponse(Long demoRequestActivityId,
                                          String comment,
                                          String description,
                                          String status,
                                          String createdByUserType,
                                          String createdBy,
                                          String createdAtDate,
                                          String createdAtTime) {
}
