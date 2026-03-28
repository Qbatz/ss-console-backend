package com.smartstay.console.utils;

import com.smartstay.console.dao.BookingsV1;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.RecurringTracker;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
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
    public static final String PLAN_NOT_FOUND = "No plan found";
    public static final String PLAN_FEATURE_NOT_FOUND = "No plan feature found";

    public static final String INVALID_ROLE_ID = "Invalid Role ID";
    public static final String INVALID_HOSTEL_ID = "Invalid hostel id";
    public static final String INVALID_SUBSCRIPTION = "Invalid subscription";
    public static final String PLAN_CODE_REQUIRED = "Plan code required";
    public static final String INVALID_PLAN_CODE = "Invalid plan code";
    public static final String HOSTEL_ID_MISMATCH = "HostelId doesn't match with payload hostelId";
    public static final String TENANT_MOBILE_MISMATCH = "Tenant mobile doesn't match with payload tenant mobile";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    public static final String PLAN_FEATURE_MISMATCH = "Plan of plan feature does not match plan";

    public static final String SUBSCRIPTION_INACTIVE = "Inactive";
    public static final String SUBSCRIPTION_ACTIVE = "Active";
    public static final String SUBSCRIPTION_NOT_ACTIVE = "Subscription is not active for this hostel";
    public static final String RECURRING_ALREADY_CREATED = "Recurring already exists this month for this hostel";
    public static final String IS_NOT_FIXED_DATE = "Type of billing is not fixed date";
    public static final String NO_BILLING_RULE_FOUND = "No billing rule found for this hostel";
    public static final String DEMO_REQUEST_STATUS_NOT_FOUND = "Demo request status not found";
    public static final String PRESENTED_BY_REQUIRED = "Presented by can't be null or empty when status is completed";
    public static final String PRESENTED_AT_REQUIRED = "Presented at can't be null when status is completed";
    public static final String HOSTEL_ID_REQUIRED = "HostelId is required";
    public static final String INVALID_BILLING_CYCLE_START_DAY = "Invalid billingCycleStartDay";
    public static final String CANNOT_USE_BILLING_CYCLE_FILTER_WITH_DATE_FILTER = "Cannot use billingCycleStartDay with filterBy";
    public static final String PLAN_FEATURE_NAME_REQUIRED = "Plan feature name is required";
    public static final String PLAN_CODE_ALREADY_EXISTS = "Plan code already exists";


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

    public static String localDateToString(LocalDate date) {
        DateTimeFormatter dateFormat =
                DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date != null ? date.format(dateFormat) : null;
    }

    public static String localTimeToString(LocalTime time) {
        DateTimeFormatter timeFormat =
                DateTimeFormatter.ofPattern("HH:mm");
        return time != null ? time.format(timeFormat) : null;
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;

        return Date.from(
                localDateTime.atZone(ZoneId.systemDefault()).toInstant()
        );
    }

    public static Set<Integer> getAllDaysOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Set<Integer> days = new HashSet<>();
        for (int i = 1; i <= maxDay; i++) {
            days.add(i);
        }

        return days;
    }

    public static Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static int calculateEndDay(int startDay, Date date) {
        LocalDate today = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        YearMonth currentMonth = YearMonth.from(today);

        int safeStartDay = Math.min(startDay, currentMonth.lengthOfMonth());

        if (safeStartDay == 1) {
            return currentMonth.lengthOfMonth();
        }

        LocalDate nextMonth = today.withDayOfMonth(safeStartDay).plusMonths(1);

        int endDay = safeStartDay - 1;

        int lastDayNextMonth = YearMonth.from(nextMonth).lengthOfMonth();
        return Math.min(endDay, lastDayNextMonth);
    }

    public static int getLastDayOfMonth(Date date) {
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return YearMonth.from(localDate).lengthOfMonth();
    }

    public static Date getDateFromDay(int day, int month, int year) {
        LocalDate localDate = LocalDate.of(year, month, 1)
                .withDayOfMonth(day);

        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static int getEndDay(int startDay, int month, int year) {

        YearMonth currentMonth = YearMonth.of(year, month);

        int safeStartDay = Math.min(startDay, currentMonth.lengthOfMonth());

        if (safeStartDay == 1) {
            return currentMonth.lengthOfMonth();
        }

        YearMonth nextMonth = currentMonth.plusMonths(1);

        int endDay = safeStartDay - 1;

        return Math.min(endDay, nextMonth.lengthOfMonth());
    }

    public static Date getEndDate(int startDay, int month, int year) {

        YearMonth currentMonth = YearMonth.of(year, month);

        int safeStartDay = Math.min(startDay, currentMonth.lengthOfMonth());

        LocalDate startDate = currentMonth.atDay(safeStartDay);

        int endDay = getEndDay(startDay, month, year);

        LocalDate endDate = (endDay < safeStartDay)
                ? startDate.plusMonths(1).withDayOfMonth(endDay)
                : startDate.withDayOfMonth(endDay);

        return Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date getNextMonthDate(int day, int month, int year) {
        YearMonth current = YearMonth.of(year, month);
        YearMonth next = current.plusMonths(1);

        int safeDay = Math.min(day, next.lengthOfMonth());

        LocalDate nextDate = next.atDay(safeDay);

        return Date.from(nextDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static boolean isEligibleForInvoice(BookingsV1 booking, int billingDay) {

        if (booking.getJoiningDate() == null) return false;

        Date joiningDate = booking.getJoiningDate();

        int joiningDay = getDayOfMonth(joiningDate);

        LocalTime autoInvoiceTime = LocalTime.of(2, 0);

        // joined after billing cycle started → skip
        if (joiningDay > billingDay) {
            return false;
        }

        // joined on same day but after invoice time → skip
        if (joiningDay == billingDay &&
                dateToLocalTime(joiningDate).isAfter(autoInvoiceTime)) {
            return false;
        }

        return true;
    }
}
