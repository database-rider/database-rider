package com.github.database.rider.core.api.replacer;

/**
 * Created by pestano on 22/07/15.
 */
public enum DayReplacerType implements ReplacerType{
    NOW(0), //
    YESTERDAY(-1),
    TOMORROW(1),
    WEEK_AFTER(7),
    WEEK_BEFORE(-7),
    MONTH_AFTER(30),
    MONTH_BEFORE(-30),
    YEAR_BEFORE(-365),
    YEAR_AFTER(+365);

    DayReplacerType(int days) {
        this.days = days;
    }

    private final int days;

    public int getDays() {
        return days;
    }

    @Override
    public String getPrefix() {
        return "DAY";
    }

    @Override
    public String getName() {
        return name();
    }
}
