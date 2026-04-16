package com.smartstay.console.dto.users;

public record AddressSnapshot(Integer addressId,
                              String houseNo,
                              String street,
                              String landMark,
                              String city,
                              String state,
                              int pincode,
                              String userId) {
}
