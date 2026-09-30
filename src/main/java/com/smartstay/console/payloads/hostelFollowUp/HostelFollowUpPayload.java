package com.smartstay.console.payloads.hostelFollowUp;

import jakarta.validation.constraints.NotBlank;

public record HostelFollowUpPayload(@NotBlank(message = "HostelId is required")
                                    String hostelId,
                                    @NotBlank(message = "Status is required")
                                    String status,
                                    @NotBlank(message = "Reason is required")
                                    String reason,
                                    String comments) {
}
