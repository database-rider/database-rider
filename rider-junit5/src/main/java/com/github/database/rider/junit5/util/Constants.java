package com.github.database.rider.junit5.util;

import com.github.database.rider.junit5.DBUnitExtension;
import org.junit.jupiter.api.extension.ExtensionContext;

public interface Constants {
    static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(DBUnitExtension.class);
    static final String JUNIT5_EXECUTOR = "junit5";
    static final String EMPTY_STRING = "";
}