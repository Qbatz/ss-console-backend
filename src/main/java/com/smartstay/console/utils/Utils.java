package com.smartstay.console.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class Utils {

    public static final String CREATED = "Created Successfully";

    public static final String UPDATED = "Updated Successfully";

    public static final String DELETED = "Deleted Successfully";
    public static final String UN_AUTHORIZED = "Unauthorized Access";

    public static final String PAYLOAD_REQUIRED = "Payload is required";

    public static final String PERMISSION_READ = "READ";
    public static final String PERMISSION_WRITE = "WRITE";
    public static final String PERMISSION_UPDATE = "UPDATE";
    public static final String PERMISSION_DELETE = "DELETE";

    public static final String ACCESS_RESTRICTED = "Access Restricted";

    public static final String ACTIVE_USERS_FOUND = "Active users found with this role, cannot delete";

    public static final String ROLE_NAME_EXISTS = "Role name already exists";
    public static final String ROLE_NAME_CANNOT_EDIT = "This role cannot be edited";
    public static final String NO_ROLES_FOUND = "No roles found";

    public static final String INVALID_ROLE_ID = "Invalid Role ID";
    public static final String INVALID_HOSTEL_ID = "Invalid hostel id";
    public static final String INVALID_SUBSCRIPTION = "Invalid subscription";
    public static final String PLAN_CODE_REQUIRED = "Plan code required";
    public static final String INVALID_PLAN_CODE = "Invalid plan code";


    public static int compareWithTwoDates(Date date1, Date date2) {
        LocalDate localDate1 = date1.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate localDate2 = date2.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return localDate1.compareTo(localDate2);
    }

    public static Date addDaysToDate(Date date, int noOfDays) {
        return Date.from(date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusDays(noOfDays)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static long findNumberOfDays(Date date1, Date date2) {
        LocalDate start = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end   = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return ChronoUnit.DAYS.between(start, end) + 1;
    }


}
