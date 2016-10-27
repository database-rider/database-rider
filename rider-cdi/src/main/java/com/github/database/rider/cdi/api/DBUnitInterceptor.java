package com.github.database.rider.cdi.api;

import com.github.database.rider.core.api.dataset.DataSet;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * Created by rafael-pestano on 22/07/2015.
 */
@InterceptorBinding
@Target({ ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DBUnitInterceptor {


}