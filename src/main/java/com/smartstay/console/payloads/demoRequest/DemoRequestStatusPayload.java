package com.smartstay.console.payloads.demoRequest;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record DemoRequestStatusPayload(@NotBlank(message = "Status can't be null or empty")
                                       String demoRequestStatus,
                                       String comments,
                                       String presentedBy,
                                       @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
                                       LocalDateTime presentedAt,
                                       String agentId,
                                       @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
                                       LocalDateTime demoFrom,
                                       @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
                                       LocalDateTime demoTo,
                                       String demoType,
                                       String demoMeetLink,
                                       String parentId,
                                       String dropReason) {
}
