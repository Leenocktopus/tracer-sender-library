package com.leandoer.sender.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@Retention(RetentionPolicy.RUNTIME)
@Target(METHOD)
public @interface Generator {
    PayloadType type() default PayloadType.NUMBER;

    String[] labels() default "";

}
