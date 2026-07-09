package com.smartstay.console.dto.kycDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DigioKycAadhaarDetails(@JsonProperty("id_number")
                                     String idNumber,

                                     @JsonProperty("document_type")
                                     String documentType,

                                     @JsonProperty("id_proof_type")
                                     String idProofType,

                                     @JsonProperty("gender")
                                     String gender,

                                     @JsonProperty("image")
                                     String image,

                                     @JsonProperty("name")
                                     String name,

                                     @JsonProperty("last_refresh_date")
                                     String lastRefreshedDate,

                                     @JsonProperty("dob")
                                     String dateOfBirth,

                                     @JsonProperty("current_address")
                                     String currentAddress,

                                     @JsonProperty("permanent_address")
                                     String permanentAddressString,

                                     @JsonProperty("permanent_address_details")
                                     DigioKycAddressDetails permanentAddress,

                                     @JsonProperty("current_address_details")
                                     DigioKycAddressDetails currentAddressDetails,

                                     @JsonProperty("completed_at")
                                     String completedAt) {
}
