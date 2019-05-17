package com.github.database.rider.core.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    private static final String DBUNIT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public static String format(Date date) {
        return new SimpleDateFormat(DBUNIT_DATE_FORMAT).format(date);
    }

    public static String format(Calendar date) {
        return format(date.getTime());
    }
}
