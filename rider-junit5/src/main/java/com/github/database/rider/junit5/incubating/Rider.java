package com.github.database.rider.junit5.incubating;

import com.github.database.rider.junit5.incubating.DBRiderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by rafael-pestano on 26/10/2016.
 * <p>
 * Shortcut to enable database rider in junit5 tests. Replaces @ExtendWith(DBUnitExtension.class) and can also be used at method level.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DBRiderExtension.class)
@Test
public @interface Rider {

    /**
     * @return name of the DataSource bean in Spring Context.
     * If empty then default dataSource will be used.
     */
    String dataSourceBeanName() default "";

}