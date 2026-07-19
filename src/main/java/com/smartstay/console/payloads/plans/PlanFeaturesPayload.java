package com.smartstay.console.payloads.plans;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PlanFeaturesPayload(@NotNull(message = "Smartstay feature Id is required")
                                  Long smartstayFeatureId,
                                  Boolean isFeatureActive,
                                  String labelText,
                                  String labelDescription,
                                  @JsonFormat(pattern = "dd-MM-yyyy")
                                  LocalDate startsFrom,
                                  @JsonFormat(pattern = "dd-MM-yyyy")
                                  LocalDate endsAt) {
}
