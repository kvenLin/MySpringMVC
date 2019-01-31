package com.clf.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @Author: clf
 * @Date: 19-1-30
 * @Description:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyService {
    String value() default"";
}
