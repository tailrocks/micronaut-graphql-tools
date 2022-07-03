package io.micronaut.graphql.tools;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;

import static io.micronaut.core.util.ArgumentUtils.requireNonNull;

@Internal
class BeanDefinitionAndMethod {

    private final BeanDefinition<?> beanDefinition;
    private final ExecutableMethod<Object, ?> executableMethod;

    BeanDefinitionAndMethod(@NonNull BeanDefinition<?> beanDefinition,
                            @NonNull ExecutableMethod<Object, ?> executableMethod) {
        requireNonNull("beanDefinition", beanDefinition);
        requireNonNull("executableMethod", executableMethod);

        this.beanDefinition = beanDefinition;
        this.executableMethod = executableMethod;
    }

    BeanDefinition<?> getBeanDefinition() {
        return beanDefinition;
    }

    ExecutableMethod<Object, ?> getExecutableMethod() {
        return executableMethod;
    }

}
