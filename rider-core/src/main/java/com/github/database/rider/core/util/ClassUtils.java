package com.github.database.rider.core.util;

public class ClassUtils {

    public static boolean isOnClasspath(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
