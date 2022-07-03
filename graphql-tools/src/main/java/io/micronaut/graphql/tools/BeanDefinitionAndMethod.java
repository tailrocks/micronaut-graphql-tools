package io.micronaut.graphql.tools;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;

import static io.micronaut.core.util.ArgumentUtils.requireNonNull;

public class BeanDefinitionAndMethod {

    private final BeanDefinition<?> beanDefinition;
    private final ExecutableMethod<Object, ?> executableMethod;

    public BeanDefinitionAndMethod(@NonNull BeanDefinition<?> beanDefinition,
                                   @NonNull ExecutableMethod<Object, ?> executableMethod) {
        requireNonNull("beanDefinition", beanDefinition);
        requireNonNull("executableMethod", executableMethod);

        this.beanDefinition = beanDefinition;
        this.executableMethod = executableMethod;
    }

    public BeanDefinition<?> getBeanDefinition() {
        return beanDefinition;
    }

    public ExecutableMethod<Object, ?> getExecutableMethod() {
        return executableMethod;
    }

}
