package com.smartstay.console.Mapper.users;

import com.smartstay.console.dao.Address;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.UserActivities;
import com.smartstay.console.dao.Users;
import com.smartstay.console.responses.hostels.OwnerHostelResponse;
import com.smartstay.console.responses.users.AddressResponse;
import com.smartstay.console.responses.users.OwnerDetailsResponse;
import com.smartstay.console.responses.users.UserActivitiesResponse;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class OwnerDetailsMapper implements Function<Users, OwnerDetailsResponse> {

    List<HostelV1> hostels;
    List<UserActivities> userActivities;

    public OwnerDetailsMapper(List<HostelV1> hostels,
                              List<UserActivities> userActivities) {
        this.hostels = hostels;
        this.userActivities = userActivities;
    }

    @Override
    public OwnerDetailsResponse apply(Users owner) {

        String firstName = owner.getFirstName() != null ? owner.getFirstName().trim() : null;
        String lastName = owner.getLastName() != null ? owner.getLastName().trim() : null;
        String fullName = Utils.getFullName(firstName, lastName);

        Address address = owner.getAddress();
        AddressResponse addressRes = null;

        if (address != null){
            addressRes = new AddressResponse(address.getAddressId(), address.getHouseNo(), address.getStreet(),
                    address.getLandMark(), address.getCity(), address.getState(), address.getPincode());
        }

        List<OwnerHostelResponse> propertiesRes = hostels.stream()
                .map(hostel -> new OwnerHostelResMapper()
                        .apply(hostel))
                .toList();

        Date lastActivityDate = userActivities.stream()
                .map(UserActivities::getCreatedAt)
                .max(Date::compareTo)
                .orElse(null);

        List<UserActivitiesResponse> activitiesRes = userActivities.stream()
                .map(activity -> new UserActivitiesResponse(
                        activity.getActivityId(), activity.getDescription(), activity.getUserId(), fullName,
                        Utils.dateToString(activity.getCreatedAt()), Utils.dateToTime(activity.getCreatedAt()),
                        activity.getSource(), activity.getActivityType()
                )).toList();


        return new OwnerDetailsResponse(owner.getUserId(), owner.getParentId(),
                firstName, lastName, fullName, Utils.getInitials(firstName, lastName),
                owner.getMobileNo(), owner.getEmailId(), owner.getProfileUrl(),
                Utils.dateToString(owner.getCreatedAt()),
                lastActivityDate != null ? Utils.dateToString(lastActivityDate) : null,
                lastActivityDate != null ? Utils.dateToTime(lastActivityDate) : null,
                addressRes, hostels.size(), propertiesRes, activitiesRes);
    }
}
