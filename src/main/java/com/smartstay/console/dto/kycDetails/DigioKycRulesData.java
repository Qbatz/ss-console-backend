package com.smartstay.console.dto.kycDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DigioKycRulesData(@JsonProperty("approval_rule")
                                List<Object> approvalRule) {
}
