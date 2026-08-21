package com.smartstay.console.responses.hostels;

public record HostelInfoRes(String hostelId,
                            String hostelName,
                            String initials,
                            String mobile,
                            String emailId,
                            String houseNo,
                            String street,
                            String landmark,
                            String city,
                            String state,
                            int country,
                            int pincode,
                            String fullAddress,
                            String mainImage) {
}
