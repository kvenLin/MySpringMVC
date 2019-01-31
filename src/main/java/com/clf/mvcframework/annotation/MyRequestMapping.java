package com.clf.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @Author: clf
 * @Date: 19-1-30
 * @Description:
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {
    String value() default"";
}
