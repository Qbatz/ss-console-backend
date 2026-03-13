package com.smartstay.console.payloads.hostel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record HostelRecDatePayload(@Min(value = 1, message = "Day must be between 1 and 28")
                                   @Max(value = 28, message = "Day must be between 1 and 28")
                                   Integer inputDay) {
}