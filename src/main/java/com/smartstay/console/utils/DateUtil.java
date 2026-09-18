package com.smartstay.console.utils;

import java.util.Date;

public class DateUtil {

    public static boolean sameDate(Date first, Date second) {

        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return Utils.compareWithTwoDates(first, second) == 0;
    }

    public static Date maxDate(Date first, Date second) {

        if (first == null) {
            return second;
        }

        if (second == null) {
            return first;
        }

        return Utils.compareWithTwoDates(first, second) >= 0
                ? first
                : second;
    }

    public static Date minDate(Date first, Date second) {

        if (first == null) {
            return second;
        }

        if (second == null) {
            return first;
        }

        return Utils.compareWithTwoDates(first, second) <= 0
                ? first
                : second;
    }
}
