package com.smartstay.console.Mapper.users;

import com.smartstay.console.dao.Users;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.utils.CountryUtils;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class UserOwnerInfoMapper implements Function<Users, OwnerInfo> {

    @Override
    public OwnerInfo apply(Users users) {

        String fullName = Utils.getFullName(users.getFirstName(), users.getLastName());
        String initials = Utils.getInitials(users.getFirstName(), users.getLastName());

        return new OwnerInfo(users.getFirstName(),
                users.getLastName(),
                fullName,
                initials,
                users.getProfileUrl(),
                users.getUserId(),
                users.getParentId(),
                CountryUtils.COUNTRY_CODE_IN,
                users.getMobileNo(),
                users.getEmailId());
    }
}
