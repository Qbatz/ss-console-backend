package com.smartstay.console.dto.kycDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DigioKycResponse(String id,

                               @JsonProperty("updated_at")
                               String updatedAt,

                               @JsonProperty("created_at")
                               String createdAt,

                               String status,

                               @JsonProperty("customer_identifier")
                               String customerIdentifier,

                               @JsonProperty("reference_id")
                               String referenceId,

                               @JsonProperty("transaction_id")
                               String transactionId,

                               @JsonProperty("customer_name")
                               String customerName,

                               @JsonProperty("expire_in_days")
                               Integer expireInDays,

                               @JsonProperty("reminder_registered")
                               Boolean reminderRegistered,

                               @JsonProperty("workflow_name")
                               String workflowName,

                               @JsonProperty("auto_approved")
                               Boolean autoApproved,

                               @JsonProperty("template_id")
                               String templateId) {
}
