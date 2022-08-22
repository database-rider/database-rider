package com.github.database.rider.core.util;

public class ObjectUtils {

    private ObjectUtils() {}

    public static <T> T defaultIfNull(T object, T defaultValue) {
        return object != null ? object : defaultValue;
    }
}
