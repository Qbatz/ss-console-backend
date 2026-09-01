package com.smartstay.console.dto.kycDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DigioKycSession(String sid,

                              @JsonProperty("is_logged_in")
                              Boolean isLoggedIn) {
}
