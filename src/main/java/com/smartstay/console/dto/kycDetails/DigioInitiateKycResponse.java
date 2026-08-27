package com.smartstay.console.dto.kycDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DigioInitiateKycResponse(String id,

                                       @JsonProperty("customer_identifier")
                                       String customerIdentifier,

                                       @JsonProperty("customer_name")
                                       String customerName,

                                       @JsonProperty("reference_id")
                                       String referenceId,

                                       @JsonProperty("transaction_id")
                                       String transactionId,

                                       String status,

                                       @JsonProperty("expire_in_days")
                                       int expiresInDays,

                                       @JsonProperty("reminder_registered")
                                       boolean reminderRegistered,

                                       @JsonProperty("workflow_name")
                                       String workflowName,

                                       @JsonProperty("auto_approved")
                                       boolean autoApproved,

                                       @JsonProperty("template_id")
                                       String templateId,

                                       @JsonProperty("created_at")
                                       String createdAt,

                                       @JsonProperty("access_token")
                                       AccessToken accessToken) {

    public record AccessToken(String id,

                              @JsonProperty("entity_id")
                              String entityId,

                              @JsonProperty("valid_till")
                              String validTill,

                              @JsonProperty("created_at")
                              String createdAt) {
    }
}
