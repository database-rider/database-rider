package com.github.database.rider.cdi.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * Created by rafael-pestano on 22/07/2015.
 */
@InterceptorBinding
@Target({ ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DBUnitInterceptor {


    /**
     * @since 1.9.0
     * @return name of the entity manager bean name.
     * If empty then default entity manager will be used.
     */
    @Nonbinding
    String entityManagerName() default "";
}