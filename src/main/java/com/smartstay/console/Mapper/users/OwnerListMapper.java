package com.smartstay.console.Mapper.users;

import com.smartstay.console.dao.Address;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.UserActivities;
import com.smartstay.console.dao.Users;
import com.smartstay.console.responses.hostels.HostelResponse;
import com.smartstay.console.responses.users.AddressResponse;
import com.smartstay.console.responses.users.OwnerResponse;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class OwnerListMapper implements Function<Users, OwnerResponse> {

    int noOfProperties;
    Address address;
    List<HostelV1> hostels;
    UserActivities userActivities;

    public OwnerListMapper(int noOfProperties, Address address,
                           List<HostelV1> hostels, UserActivities userActivities) {
        this.noOfProperties = noOfProperties;
        this.address = address;
        this.hostels = hostels;
        this.userActivities = userActivities;
    }

    @Override
    public OwnerResponse apply(Users users) {

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

        List<HostelResponse> hostelList = hostels.stream()
                .map(hostel -> new HostelResponse(
                        hostel.getHostelId(),
                        hostel.getHostelName()
                )).toList();

        Date latestActivityDate = null;
        if (userActivities != null){
            latestActivityDate = userActivities.getCreatedAt();
        }

        return new OwnerResponse(users.getUserId(), users.getParentId(), firstName,
                lastName, fullName.toString(), initials.toString(), users.getMobileNo(),
                noOfProperties, addressRes, Utils.dateToString(users.getCreatedAt()),
                latestActivityDate != null ? Utils.dateToString(latestActivityDate) : null,
                latestActivityDate != null ? Utils.dateToTime(latestActivityDate) : null,
                hostelList);
    }
}
