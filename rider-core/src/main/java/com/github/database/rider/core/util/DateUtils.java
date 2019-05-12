package com.github.database.rider.core.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    private static final SimpleDateFormat DBUNIT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static synchronized String format(Date date) {
        return DBUNIT_DATE_FORMAT.format(date);
    }

    public static synchronized String format(Calendar date) {
        return format(date.getTime());
    }
}
