package com.smartstay.console.responses.hostelRelationalAgent;

public record RelationalHostelsResponse(String hostelId,
                                        String hostelName,
                                        String initials,
                                        String mobile,
                                        String houseNo,
                                        String street,
                                        String landmark,
                                        String city,
                                        String state,
                                        int country,
                                        int pincode,
                                        String fullAddress,
                                        String mainImage,
                                        String planCode,
                                        String planName,
                                        String planEndsAtDate,
                                        String planEndsAtTime,
                                        long expiringInDays,
                                        boolean aboutToExpire) {
}
