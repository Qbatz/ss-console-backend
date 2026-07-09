package com.smartstay.console.dto.kycDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DigioKycAddressDetails(@JsonProperty("address")
                                     String address,

                                     @JsonProperty("locality_or_post_office")
                                     String localityOrPostOffice,

                                     @JsonProperty("district_or_city")
                                     String districtOrCity,

                                     @JsonProperty("state")
                                     String state,

                                     @JsonProperty("pincode")
                                     String pincode) {
}
