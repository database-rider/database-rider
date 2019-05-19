package com.github.database.rider.core.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    private static final String DBUNIT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String DBUNIT_DATE_FORMAT = "yyyy-MM-dd";


    public static String format(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if(calendar.get(Calendar.HOUR) == 0 && calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0) {
            return formatDate(calendar);
        } else {
            return formatDateTime(calendar);
        }
    }

    public static String format(Calendar calendar) {
        if (calendar.get(Calendar.HOUR) == 0 && calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0) {
            return formatDate(calendar);
        } else {
            return formatDateTime(calendar);
        }
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat(DBUNIT_DATE_FORMAT).format(date);
    }

    public static String formatDate(Calendar date) {
        return formatDate(date.getTime());
    }

    public static String formatDateTime(Date date) {
        return new SimpleDateFormat(DBUNIT_DATE_TIME_FORMAT).format(date);
    }

    public static String formatDateTime(Calendar date) {
        return formatDateTime(date.getTime());
    }
}
