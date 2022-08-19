package com.github.database.rider.core.api.replacer;

/**
 * Created by pestano on 22/07/15.
 */
public enum HourReplacerType implements ReplacerType{
    NOW(0),
    PLUS_ONE(1),
    MINUS_ONE(-1),
    PLUS_TEN(+10),
    MINUS_TEN(-10);

    HourReplacerType(int hours) {
        this.hours = hours;
    }

    private final int hours;

    public int getHours() {
        return hours;
    }

    @Override
    public String getPrefix() {
        return "HOUR";
    }

    @Override
    public String getName() {
        return name();
    }


}
