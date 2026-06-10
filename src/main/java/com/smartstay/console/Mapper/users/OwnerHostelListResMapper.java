package com.smartstay.console.Mapper.users;

import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.HotelType;
import com.smartstay.console.dao.Users;
import com.smartstay.console.responses.hostels.HostelListOwnerResponse;
import com.smartstay.console.responses.users.OwnerHostelListResponse;
import com.smartstay.console.responses.users.UsersListHostelResponse;
import com.smartstay.console.utils.Utils;

import java.util.*;
import java.util.function.Function;

public class OwnerHostelListResMapper implements Function<Users, OwnerHostelListResponse> {

    List<HostelV1> hostels;
    Map<Integer, HotelType> hotelTypeMap;
    Map<String, Set<String>> hostelToUserIdsMap;
    Map<String, Users> usersMap;

    public OwnerHostelListResMapper(List<HostelV1> hostels,
                                    Map<Integer, HotelType> hotelTypeMap,
                                    Map<String, Set<String>> hostelToUserIdsMap,
                                    Map<String, Users> usersMap) {
        this.hostels = hostels;
        this.hotelTypeMap = hotelTypeMap;
        this.hostelToUserIdsMap = hostelToUserIdsMap;
        this.usersMap = usersMap;
    }

    @Override
    public OwnerHostelListResponse apply(Users users) {

        List<HostelListOwnerResponse> hostelResponses = new ArrayList<>();

        if (hostels != null) {
            for (HostelV1 hostel : hostels) {

                List<UsersListHostelResponse> staffsRes = new ArrayList<>();
                if (hostelToUserIdsMap != null) {
                    Set<String> userIds = hostelToUserIdsMap.getOrDefault(
                            hostel.getHostelId(),
                            Collections.emptySet()
                    );

                    for (String userId : userIds) {
                        Users user = usersMap.getOrDefault(userId, null);
                        if (user != null) {
                            UsersListHostelResponse staff = new UsersListHostelResponse(user.getUserId(),
                                    user.getParentId(), user.getFirstName(), user.getLastName(),
                                    Utils.getFullName(user.getFirstName(), user.getLastName()),
                                    Utils.getInitials(user.getFirstName(), user.getLastName()),
                                    user.getProfileUrl(), user.getMobileNo(), user.getEmailId());

                            staffsRes.add(staff);
                        }
                    }
                }

                String hostelType = null;

                if (hotelTypeMap != null) {
                    HotelType hotelType = hotelTypeMap.getOrDefault(hostel.getHostelType(), null);
                    if (hotelType != null) {
                        hostelType = hotelType.getType();
                    }
                }

                HostelListOwnerResponse hostelRes = new HostelListOwnerResponse(hostel.getHostelId(),
                        hostelType, hostel.getHostelName(), Utils.getInitials(hostel.getHostelName()),
                        hostel.getMobile(), hostel.getHouseNo(), hostel.getStreet(), hostel.getLandmark(),
                        hostel.getCity(), hostel.getState(), hostel.getCountry(), hostel.getPincode(),
                        Utils.buildFullAddress(hostel), hostel.getMainImage(), staffsRes);

                hostelResponses.add(hostelRes);
            }
        }

        return new OwnerHostelListResponse(users.getFirstName(), users.getLastName(),
                Utils.getFullName(users.getFirstName(), users.getLastName()),
                Utils.getInitials(users.getFirstName(), users.getLastName()),
                users.getProfileUrl(), users.getUserId(), users.getParentId(),
                users.getMobileNo(), users.getEmailId(), hostelResponses);
    }
}
