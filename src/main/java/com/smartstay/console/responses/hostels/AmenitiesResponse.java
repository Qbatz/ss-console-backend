package com.smartstay.console.responses.hostels;

public record AmenitiesResponse(String amenityId,
                                String amenityName,
                                Double amenityAmount,
                                String description,
                                String termsAndCondition,
                                Boolean isProRate) {
}
