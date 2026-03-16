package com.smartstay.console.payloads.demoRequest;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record DemoRequestPayload(@NotBlank(message = "Name can't be null or empty")
                                 String name,

                                 @Email(message = "Invalid email format")
                                 String email,

                                 @NotBlank(message = "ContactNo can't be null or empty")
                                 @Pattern(regexp = "^[0-9]{10}$", message = "ContactNo must be 10 digits")
                                 String contactNo,

                                 @NotBlank(message = "CountryCode can't be null or empty")
                                 String countryCode,

                                 String organization,

                                 @NotNull(message = "NoOfHostels can't be null")
                                 @Positive
                                 Integer noOfHostels,

                                 @NotNull(message = "NoOfTenants can't be null")
                                 @Positive
                                 Integer noOfTenants,

                                 String city,
                                 String state,
                                 String country,
                                 String comments,

                                 @FutureOrPresent(message = "Date must be from future or today")
                                 @JsonFormat(pattern = "dd-MM-yyyy")
                                 LocalDate requestedDate,

                                 @JsonFormat(pattern = "HH:mm")
                                 LocalTime requestedTime) {
}
