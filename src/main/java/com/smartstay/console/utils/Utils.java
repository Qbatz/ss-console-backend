package com.smartstay.console.utils;

import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.RecurringTracker;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Utils {

    public static final String OUTPUT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String OUTPUT_TIME_FORMAT = "hh:mm:ss a";
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
    public static final String NO_HOSTEL_FOUND = "No hostel found";
    public static final String NO_TENANT_HOSTEL_FOUND = "No tenant with hostel found";
    public static final String NO_AGENT_FOUND = "No agent found";
    public static final String NO_OWNER_FOUND = "No owner found";
    public static final String DEMO_REQUEST_NOT_FOUND = "Demo request not found";

    public static final String INVALID_ROLE_ID = "Invalid Role ID";
    public static final String INVALID_HOSTEL_ID = "Invalid hostel id";
    public static final String INVALID_SUBSCRIPTION = "Invalid subscription";
    public static final String PLAN_CODE_REQUIRED = "Plan code required";
    public static final String INVALID_PLAN_CODE = "Invalid plan code";
    public static final String HOSTEL_ID_MISMATCH = "HostelId doesn't match with payload hostelId";
    public static final String TENANT_MOBILE_MISMATCH = "Tenant mobile doesn't match with payload tenant mobile";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";

    public static final String SUBSCRIPTION_INACTIVE = "Inactive";
    public static final String SUBSCRIPTION_ACTIVE = "Active";
    public static final String SUBSCRIPTION_NOT_ACTIVE = "Subscription is not active for this hostel";
    public static final String RECURRING_ALREADY_CREATED = "Recurring already exists this month for this hostel";
    public static final String IS_NOT_FIXED_DATE = "Type of billing is not fixed date";
    public static final String DAY_NOT_MATCH = "Today or the input day doesn't match with the billing rule day";
    public static final String BILLING_DAY_NOT_REACHED = "This month's billing day has not reached";
    public static final String NO_BILLING_RULE_FOUND = "No billing rule found for this hostel";


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

    public static String dateToString(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(OUTPUT_DATE_FORMAT).format(date);
    }

    public static String dateToTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(OUTPUT_TIME_FORMAT).format(date);
    }

    public static String getFullName(String firstName, String lastName){
        StringBuilder fullName = new StringBuilder();

        if (firstName != null) {
            firstName = firstName.trim();
            fullName.append(firstName);
        }

        if (lastName != null && !lastName.isBlank()) {
            lastName = lastName.trim();
            fullName.append(" ");
            fullName.append(lastName);
        }

        return fullName.toString();
    }

    public static String getInitials(String firstName, String lastName){
        StringBuilder initials = new StringBuilder();

        if (firstName != null) {
            firstName = firstName.trim();
            initials.append(firstName.toUpperCase().charAt(0));
        }

        if (lastName != null && !lastName.isBlank()) {
            lastName = lastName.trim();
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

        return initials.toString();
    }

    public static String getInitials(String name){
        StringBuilder initials = new StringBuilder();

        if (name != null) {
            String[] arrName = name.split(" ");
            if (arrName.length > 0) {
                initials.append(arrName[0].toUpperCase().charAt(0));
            }
            if (arrName.length > 1) {
                initials.append(arrName[arrName.length - 1].toUpperCase().charAt(0));
            }
            else {
                String lastPart = arrName[arrName.length - 1].toUpperCase();

                if (lastPart.length() > 1) {
                    initials.append(lastPart.charAt(1));
                }
            }
        }

        return initials.toString();
    }

    public static String buildFullAddress(HostelV1 hostelV1) {
        StringBuilder fullAddress = new StringBuilder();

        if (hostelV1.getHouseNo() != null &&
                !hostelV1.getHouseNo().trim().equalsIgnoreCase("")) {
            fullAddress.append(hostelV1.getHouseNo());
        }
        if (hostelV1.getStreet() != null) {
            if (fullAddress.isEmpty()) {
                fullAddress.append(hostelV1.getStreet());
            }
            else {
                fullAddress.append(", ");
                fullAddress.append(hostelV1.getStreet());
            }
        }
        if (hostelV1.getCity() != null) {
            if (fullAddress.isEmpty()) {
                fullAddress.append(hostelV1.getCity());
            }
            else {
                fullAddress.append(", ");
                fullAddress.append(hostelV1.getCity());
            }
        }
        if (hostelV1.getState() != null) {
            if (fullAddress.isEmpty()) {
                fullAddress.append(hostelV1.getState());
            }
            else {
                fullAddress.append(", ");
                fullAddress.append(hostelV1.getState());
            }
        }
        return fullAddress.toString();
    }

    public static String formatDateDisplay(Date date) {
        if (date == null) return "";

        long now = System.currentTimeMillis();
        long diffMillis = now - date.getTime();

        long minutes = diffMillis / (60 * 1000);
        long hours = diffMillis / (60 * 60 * 1000);

        if (hours < 24) {
            if (hours >= 1) {
                return hours + " hrs ago";
            }
            if (minutes >= 1) {
                return minutes + " mins ago";
            }
            return "Just now";
        }

        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    public static Integer getDayOfMonth(Date date) {
        if (date == null) return null;

        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return localDate.getDayOfMonth();
    }

    public static Integer getYesterdayDayOfMonth(Date date) {
        if (date == null) return null;

        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .minusDays(1);

        return localDate.getDayOfMonth();
    }

    public static Integer getTwoDaysAgoDayOfMonth(Date date) {
        if (date == null) return null;

        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .minusDays(2);

        return localDate.getDayOfMonth();
    }

    public static Integer getTomorrowDayOfMonth(Date date) {
        if (date == null) return null;

        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusDays(1);

        return localDate.getDayOfMonth();
    }

    public static Set<Integer> getThisWeekDays(Date date) {

        if (date == null) return Collections.emptySet();

        LocalDate today = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        Set<Integer> days = new HashSet<>();

        for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
            days.add(d.getDayOfMonth());
        }

        return days;
    }

    public static Set<Integer> getLastWeekDays(Date date) {

        if (date == null) return Collections.emptySet();

        LocalDate today = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate monday = today.with(DayOfWeek.MONDAY).minusWeeks(1);
        LocalDate sunday = monday.plusDays(6);

        Set<Integer> days = new HashSet<>();

        for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
            days.add(d.getDayOfMonth());
        }

        return days;
    }

    public static Set<Integer> getDaysTillToday(Date date) {

        if (date == null) return Collections.emptySet();

        int today = getDayOfMonth(date);

        Set<Integer> days = new HashSet<>();

        for (int i = 1; i <= today; i++) {
            days.add(i);
        }

        return days;
    }

    public static Set<Integer> getUpcomingDays(Date date) {

        if (date == null) return Collections.emptySet();

        LocalDate today = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        int todayDay = today.getDayOfMonth();
        int lastDay = today.lengthOfMonth();

        Set<Integer> days = new HashSet<>();

        for (int i = todayDay + 1; i <= lastDay; i++) {
            days.add(i);
        }

        return days;
    }

    public static Date findLastDate(Integer cycleStartDay, Date date) {
        LocalDate today = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        ;
        LocalDate startDate = LocalDate.of(today.getYear(), today.getMonth(), cycleStartDay);

        LocalDate cycleEnd;
        if (cycleStartDay == 1) {
            YearMonth ym = YearMonth.from(startDate);
            cycleEnd = ym.atEndOfMonth();
        } else {
            LocalDate nextMonth = startDate.plusMonths(1);
            int endDay = cycleStartDay - 1;

            int lastDayOfNextMonth = YearMonth.from(nextMonth).lengthOfMonth();
            if (endDay > lastDayOfNextMonth) {
                endDay = lastDayOfNextMonth;
            }

            cycleEnd = nextMonth.withDayOfMonth(endDay);
        }

        return Date.from(cycleEnd.atStartOfDay(ZoneId.systemDefault()).toInstant());

    }

    public static Double roundOfDouble(double number) {
        return (double) Math.round(number);
    }

    public static int findDateFromDate(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            return cal.get(Calendar.DAY_OF_MONTH);
        }
        return 0;
    }

    public static Integer getCurrentMonth(Date date) {
        if (date == null) return null;

        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return localDate.getMonth().getValue();
    }

    public static Integer getCurrentYear(Date date) {
        if (date == null) return null;

        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return localDate.getYear();
    }

    public static boolean isSameBillingCycle(int billingStartDay, RecurringTracker tracker) {

        if (tracker == null || tracker.getCreationDay() == null ||
                tracker.getCreationMonth() == null || tracker.getCreationYear() == null) {
            return false;
        }

        LocalDate today = LocalDate.now();

        return tracker.getCreationDay() == billingStartDay
                && tracker.getCreationMonth() == today.getMonthValue()
                && tracker.getCreationYear() == today.getYear();
    }

    public static LocalTime dateToLocalTime(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime dateTime = date
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return dateTime.toLocalTime();
    }
}
