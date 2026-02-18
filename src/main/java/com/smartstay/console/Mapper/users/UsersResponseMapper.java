package com.smartstay.console.Mapper.users;

import com.smartstay.console.dao.Address;
import com.smartstay.console.dao.Users;
import com.smartstay.console.responses.users.AddressResponse;
import com.smartstay.console.responses.users.UsersResponse;

import java.util.function.Function;

public class UsersResponseMapper implements Function<Users, UsersResponse> {

    Address address;

    public UsersResponseMapper(Address address) {
        this.address = address;
    }

    @Override
    public UsersResponse apply(Users users) {
        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        String firstName = null;
        String lastName = null;

        if (users.getFirstName() != null) {
            firstName = users.getFirstName().trim();
            fullName.append(firstName);
            initials.append(firstName.toUpperCase().charAt(0));
        }

        if (users.getLastName() != null && !users.getLastName().isBlank()) {
            lastName = users.getLastName().trim();
            fullName.append(" ");
            fullName.append(lastName);
            initials.append(lastName.toUpperCase().charAt(0));
        }
        else {
            if (firstName != null) {
                String[] nameArr = firstName.split(" ");
                if (nameArr.length > 1) {
                    initials.append(nameArr[nameArr.length - 1].toUpperCase().charAt(0));
                }
                else {
                    String lastPart = nameArr[nameArr.length - 1].toUpperCase();

                    if (lastPart.length() > 1) {
                        initials.append(lastPart.charAt(1));
                    }
                }
            }
        }

        AddressResponse addressRes = null;

        if (address != null){
            addressRes = new AddressResponse(address.getAddressId(), address.getHouseNo(), address.getStreet(),
                    address.getLandMark(), address.getCity(), address.getState(), address.getPincode());
        }

        return new UsersResponse(users.getUserId(), users.getParentId(), firstName,
                lastName, fullName.toString(), initials.toString(), users.getMobileNo(),
                addressRes);
    }
}
