package com.github.database.rider.api.replacer;

/**
 * Created by pestano on 22/07/15.
 */
public enum SecondReplacerType implements ReplacerType{
    NOW(0),
    PLUS_ONE(1),
    MINUS_ONE(-1),
    PLUS_TEN(+10),
    MINUS_TEN(-10),
    PLUS_30(+30),
    MINUS_30(-30);

    SecondReplacerType(int seconds) {
        this.seconds = seconds;
    }

    private final int seconds;

    public int getSeconds() {
        return seconds;
    }

    @Override
    public String getPerfix() {
        return "SEC";
    }

    @Override
    public String getName() {
        return name();
    }

}
