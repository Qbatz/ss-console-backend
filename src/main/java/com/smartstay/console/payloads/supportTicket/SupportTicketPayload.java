package com.smartstay.console.payloads.supportTicket;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record SupportTicketPayload(@NotBlank(message = "ParentId is required")
                                   String parentId,
                                   @NotBlank(message = "HostelId is required")
                                   String hostelId,
                                   @NotBlank(message = "RaisedBy is required")
                                   String raisedBy,
                                   @NotBlank(message = "Query type is required")
                                   String queryType,
                                   @NotBlank(message = "Subject is required")
                                   String subject,
                                   @NotNull(message = "Issue date is required")
                                   @PastOrPresent(message = "Issue date cannot be in the future")
                                   @JsonFormat(pattern = "dd-MM-yyyy")
                                   LocalDate issueDate,
                                   String remarks) {
}
