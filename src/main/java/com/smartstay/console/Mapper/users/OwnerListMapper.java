package com.smartstay.console.Mapper.users;

import com.smartstay.console.dao.UserActivities;
import com.smartstay.console.dto.users.OwnerWithAddressProjection;
import com.smartstay.console.responses.users.AddressResponse;
import com.smartstay.console.responses.users.OwnerResponse;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.function.Function;

public class OwnerListMapper implements Function<OwnerWithAddressProjection, OwnerResponse> {

    int noOfProperties;
    UserActivities userActivities;

    public OwnerListMapper(int noOfProperties,
                           UserActivities userActivities) {
        this.noOfProperties = noOfProperties;
        this.userActivities = userActivities;
    }

    @Override
    public OwnerResponse apply(OwnerWithAddressProjection owner) {

        String fullName = Utils.getFullName(owner.getFirstName(), owner.getLastName());
        String initials = Utils.getInitials(owner.getFirstName(), owner.getLastName());

        AddressResponse addressRes = null;

        if (owner.getAddressId() != null){
            addressRes = new AddressResponse(owner.getAddressId(), owner.getHouseNo(), owner.getStreet(),
                    owner.getLandMark(), owner.getCity(), owner.getState(), owner.getPincode());
        }

        Date latestActivityDate = null;
        if (userActivities != null){
            latestActivityDate = userActivities.getCreatedAt();
        }

        return new OwnerResponse(owner.getUserId(), owner.getParentId(), owner.getFirstName(),
                owner.getLastName(), fullName, initials, owner.getMobileNo(), noOfProperties,
                addressRes, Utils.dateToString(owner.getCreatedAt()),
                latestActivityDate != null ? Utils.dateToString(latestActivityDate) : null,
                latestActivityDate != null ? Utils.dateToTime(latestActivityDate) : null
        );
    }
}
