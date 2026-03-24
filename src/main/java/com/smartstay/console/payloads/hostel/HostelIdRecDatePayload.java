package com.smartstay.console.payloads.hostel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HostelIdRecDatePayload(@NotBlank(message = "HostelId is required")
                                     String hostelId
//                                     @NotNull(message = "InputDay is required")
//                                     @Min(value = 1, message = "Day must be between 1 and 28")
//                                     @Max(value = 28, message = "Day must be between 1 and 28")
//                                     Integer inputDay
                                   ) {
}
