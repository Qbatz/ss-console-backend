package com.smartstay.console.responses.users;

public record AddressResponse(int addressId,
                              String houseNo,
                              String street,
                              String landMark,
                              String city,
                              String state,
                              int pincode) {
}
