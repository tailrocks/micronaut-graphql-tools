package io.github.expatiat.micronaut.graphql.tools;

import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexey Zhokhov
 */
public class ResolverItem {

    private final BeanDefinition beanDefinition;
    private final Set<ExecutableMethod> executableMethods = new HashSet<>();

    public ResolverItem(BeanDefinition beanDefinition) {
        this.beanDefinition = beanDefinition;
    }

    public void addExecutableMethod(ExecutableMethod method) {
        executableMethods.add(method);
    }

    public BeanDefinition getBeanDefinition() {
        return beanDefinition;
    }

    public Set<ExecutableMethod> getExecutableMethods() {
        return executableMethods;
    }

}
