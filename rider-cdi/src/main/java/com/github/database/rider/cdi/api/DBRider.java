package com.github.database.rider.cdi.api;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * @since 1.8.0
 * Created by rafael-pestano on 30/11/2019.
 *
 * Just an alias annotation for @DBUnitInterceptor, see issue #164
 */
@InterceptorBinding
@Target({ ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DBUnitInterceptor
public @interface DBRider {


    /**
     * @since 1.9.0
     * @return name of the entity manager bean name.
     * If empty then default entity manager will be used.
     */
    @Nonbinding
    String entityManagerName() default "";

}