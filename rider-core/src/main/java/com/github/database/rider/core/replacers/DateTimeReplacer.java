package com.github.database.rider.core.replacers;

import com.github.database.rider.core.api.replacer.*;
import com.github.database.rider.core.util.DateUtils;
import org.dbunit.dataset.ReplacementDataSet;

import java.util.Calendar;
import java.util.Date;

/**
 * based on: http://marcin-michalski.pl/2012/10/22/decorating-dbunit-datasets-power-of-replacementdataset/
 */
public class DateTimeReplacer implements Replacer {

    public static final String PLACEHOLDER_FORMAT = "[%s,%s]"; // [prefix:placeholderName]


    @Override
    public void addReplacements(ReplacementDataSet dataSet) {
        Date currentDate = new Date();
        replaceDays(currentDate, dataSet);
        replaceHours(currentDate, dataSet);
        replaceMinutes(currentDate, dataSet);
        replaceSeconds(currentDate, dataSet);
    }

    private void replaceDays(Date currentDate, ReplacementDataSet replacementDataSet) {
        for (DayReplacerType type : DayReplacerType.values()) {
            Date calculatedDate = addDays(currentDate, type.getDays());
            replacementDataSet.addReplacementSubstring(getPlaceholderPattern(type), DateUtils.formatDateTime(calculatedDate));
        }
    }

    private void replaceHours(Date currentDate, ReplacementDataSet replacementDataSet) {
        for (HourReplacerType type : HourReplacerType.values()) {
            Date calculatedDate = addHours(currentDate, type.getHours());
            replacementDataSet.addReplacementSubstring(getPlaceholderPattern(type), DateUtils.formatDateTime(calculatedDate));
        }
    }

    private void replaceMinutes(Date currentDate, ReplacementDataSet replacementDataSet) {
        for (MinuteReplacerType type : MinuteReplacerType.values()) {
            Date calculatedDate = addMinutes(currentDate, type.getMinutes());
            replacementDataSet.addReplacementSubstring(getPlaceholderPattern(type), DateUtils.formatDateTime(calculatedDate));
        }
    }

    private void replaceSeconds(Date currentDate, ReplacementDataSet replacementDataSet) {
        for (SecondReplacerType type : SecondReplacerType.values()) {
            Date calculatedDate = addSeconds(currentDate, type.getSeconds());
            replacementDataSet.addReplacementSubstring(getPlaceholderPattern(type), DateUtils.formatDateTime(calculatedDate));
        }
    }

    private String getPlaceholderPattern(ReplacerType replacerType) {
        return String.format(DateTimeReplacer.PLACEHOLDER_FORMAT, replacerType.getPerfix(), replacerType.getName());
    }

    private Date addMinutes(Date currentDate, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.MINUTE,minutes);
        return calendar.getTime();
    }

    private Date addDays(Date currentDate, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH,days);
        return calendar.getTime();
    }

    private Date addHours(Date currentDate, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.HOUR_OF_DAY,hours);
        return calendar.getTime();
    }

    private Date addSeconds(Date currentDate, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.SECOND,seconds);
        return calendar.getTime();
    }


}
