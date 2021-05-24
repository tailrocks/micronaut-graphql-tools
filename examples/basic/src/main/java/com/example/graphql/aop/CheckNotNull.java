package com.example.graphql.aop;

import io.micronaut.aop.Around;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Alexey Zhokhov
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Around
public @interface CheckNotNull {
}
