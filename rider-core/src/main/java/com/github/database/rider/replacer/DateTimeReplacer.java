package com.github.database.rider.replacer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.github.database.rider.api.replacer.*;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;

/**
 * based on: http://marcin-michalski.pl/2012/10/22/decorating-dbunit-datasets-power-of-replacementdataset/
 */
public class DateTimeReplacer {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String PLACEHOLDER_FORMAT = "[%s,%s]"; // [prefix:placeholderName]

    private static DateTimeReplacer instance;

    private DateTimeReplacer(){

    }

    public static IDataSet replace(IDataSet dataset) {
        if (instance == null){
            instance = new DateTimeReplacer();
        }
        Date currentDate = new Date();
        ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataset);
        instance.replaceDays(currentDate, replacementDataSet);
        instance.replaceHours(currentDate, replacementDataSet);
        instance.replaceMinutes(currentDate, replacementDataSet);
        instance.replaceSeconds(currentDate, replacementDataSet);
        return replacementDataSet;
    }

    private void replaceDays(Date currentDate, ReplacementDataSet replacementDataSet) {
        for (DayReplacerType type : DayReplacerType.values()) {
            Date calculatedDate = addDays(currentDate, type.getDays());
            replacementDataSet.addReplacementSubstring(instance.getPlaceholderPattern(type), FORMAT.format(calculatedDate));
        }
    }

    private void replaceHours(Date currentDate, ReplacementDataSet replacementDataSet) {
        for (HourReplacerType type : HourReplacerType.values()) {
            Date calculatedDate = addHours(currentDate, type.getHours());
            replacementDataSet.addReplacementSubstring(instance.getPlaceholderPattern(type), FORMAT.format(calculatedDate));
        }
    }

    private void replaceMinutes(Date currentDate, ReplacementDataSet replacementDataSet) {
        for (MinuteReplacerType type : MinuteReplacerType.values()) {
            Date calculatedDate = addMinutes(currentDate, type.getMinutes());
            replacementDataSet.addReplacementSubstring(instance.getPlaceholderPattern(type), FORMAT.format(calculatedDate));
        }
    }

    private void replaceSeconds(Date currentDate, ReplacementDataSet replacementDataSet) {
        for (SecondReplacerType type : SecondReplacerType.values()) {
            Date calculatedDate = addSeconds(currentDate, type.getSeconds());
            replacementDataSet.addReplacementSubstring(instance.getPlaceholderPattern(type), FORMAT.format(calculatedDate));
        }
    }

    private String getPlaceholderPattern(ReplacerType replacerType) {
        return String.format(DateTimeReplacer.PLACEHOLDER_FORMAT, replacerType.getPerfix(), replacerType.getName());
    }

    public Date addMinutes(Date currentDate, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.MINUTE,minutes);
        return calendar.getTime();
    }

    public Date addDays(Date currentDate, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH,days);
        return calendar.getTime();
    }

    public Date addHours(Date currentDate, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.HOUR_OF_DAY,hours);
        return calendar.getTime();
    }

    public Date addSeconds(Date currentDate, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.SECOND,seconds);
        return calendar.getTime();
    }


}
