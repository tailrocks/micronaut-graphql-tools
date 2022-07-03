package io.micronaut.graphql.tools;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;

import java.util.HashSet;
import java.util.Set;

import static io.micronaut.core.util.ArgumentUtils.requireNonNull;

public class BeanDefinitionAndMethods {
    private final BeanDefinition<?> beanDefinition;
    private final Set<ExecutableMethod<Object, ?>> executableMethods = new HashSet<>();

    public BeanDefinitionAndMethods(@NonNull BeanDefinition<?> beanDefinition) {
        requireNonNull("beanDefinition", beanDefinition);

        this.beanDefinition = beanDefinition;
    }

    public void addExecutableMethod(@NonNull ExecutableMethod<Object, ?> executableMethod) {
        requireNonNull("executableMethod", executableMethod);

        executableMethods.add(executableMethod);
    }

    public BeanDefinition<?> getBeanDefinition() {
        return beanDefinition;
    }

    public Set<ExecutableMethod<Object, ?>> getExecutableMethods() {
        return executableMethods;
    }

}
