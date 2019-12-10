package com.github.database.rider.cdi.api;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * @since 1.9.0
 * Created by rafael-pestano on 10/12/2019.
 *
 */
@Qualifier
@Any
@Default
@Inherited
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RiderPU {

    String value() default "";

}