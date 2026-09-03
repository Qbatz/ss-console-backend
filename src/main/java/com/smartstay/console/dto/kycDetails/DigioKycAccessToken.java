package com.smartstay.console.dto.kycDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DigioKycAccessToken(String id,

                                  @JsonProperty("entity_id")
                                  String entityId,

                                  @JsonProperty("valid_till")
                                  String validTill,

                                  @JsonProperty("created_at")
                                  String createdAt) {
}
