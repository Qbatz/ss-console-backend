package com.smartstay.console.Mapper.users;

import com.smartstay.console.dao.Users;
import com.smartstay.console.responses.hostels.OwnerInfo;
import org.springframework.security.core.parameters.P;

import java.util.function.Function;

public class UserOnerInfoMapper implements Function<Users, OwnerInfo> {
    @Override
    public OwnerInfo apply(Users users) {
        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();

        if (users.getFirstName() != null) {
            fullName.append(users.getFirstName());
            initials.append(users.getFirstName().toUpperCase().charAt(0));
        }

        if (users.getLastName() != null && !users.getLastName().trim().equalsIgnoreCase("")) {
            fullName.append(" ");
            fullName.append(users.getLastName());
            initials.append(users.getLastName().toUpperCase().charAt(0));
        }
        else {
            if (users.getFirstName() != null) {
                String[] nameArr = users.getFirstName().split(" ");
                if (nameArr.length > 1) {
                    initials.append(nameArr[nameArr.length - 1].toUpperCase().charAt(0));
                }
                else {
                    initials.append(nameArr[nameArr.length - 1].toUpperCase().charAt(1));
                }
            }
        }

        return new OwnerInfo(users.getFirstName(),
                users.getLastName(),
                fullName.toString(),
                initials.toString(),
                users.getProfileUrl(),
                users.getUserId(),
                users.getParentId());
    }
}
