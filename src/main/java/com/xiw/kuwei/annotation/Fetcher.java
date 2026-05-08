package com.xiw.kuwei.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Fetcher {

    int type() default Integer.MIN_VALUE;

    int order() default Integer.MIN_VALUE;

    String platform();

}
