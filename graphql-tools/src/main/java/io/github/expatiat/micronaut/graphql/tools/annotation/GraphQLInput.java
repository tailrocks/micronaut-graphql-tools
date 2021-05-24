package io.github.expatiat.micronaut.graphql.tools.annotation;

import io.micronaut.core.annotation.Introspected;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Alexey Zhokhov
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.TYPE})
@Introspected
public @interface GraphQLInput {

}
