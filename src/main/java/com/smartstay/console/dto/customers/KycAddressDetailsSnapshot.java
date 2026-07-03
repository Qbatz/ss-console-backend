package com.smartstay.console.dto.customers;

public record KycAddressDetailsSnapshot(Long id,
                                        String currentAddress,
                                        String currentLocality,
                                        String currentCity,
                                        String currentState,
                                        String currentPincode,
                                        String permanentAddress,
                                        String permanentLocality,
                                        String permanentCity,
                                        String permanentState,
                                        String permanentPincode,
                                        Long kycDetailsId) {
}
