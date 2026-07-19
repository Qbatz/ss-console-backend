package com.smartstay.console.dto.kycDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DigioKycDetails(@JsonProperty("aadhaar")
                              DigioKycAadhaarDetails aadhaarDetails) {
}
