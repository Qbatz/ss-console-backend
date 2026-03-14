package com.smartstay.console.dto.users;

import java.util.Date;

public interface OwnerWithAddressProjection {
    String getUserId();
    String getParentId();
    String getFirstName();
    String getLastName();
    String getMobileNo();
    Date getCreatedAt();

    Integer getAddressId();
    String getHouseNo();
    String getStreet();
    String getLandMark();
    String getCity();
    String getState();
    Integer getPincode();
}
