package com.example.graphql.aop;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;

import javax.inject.Singleton;

/**
 * @author Alexey Zhokhov
 */
@Singleton
@InterceptorBean(CheckNotNull.class)
public class CheckNotNullInterceptor implements MethodInterceptor<Object, Object> {

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        Object result = context.proceed();

        if (result == null) {
            throw new RuntimeException("Null result is not allowed");
        }

        return result;
    }

}
