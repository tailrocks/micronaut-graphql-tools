package io.micronaut.graphql.tools;

import io.micronaut.context.annotation.Infrastructure;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver;
import io.micronaut.graphql.tools.exceptions.IncorrectAnnotationException;
import io.micronaut.graphql.tools.exceptions.MethodNotFoundException;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Internal
@Singleton
@Infrastructure
public class GraphQLResolversRegistry {

    private final List<BeanDefinitionAndMethods> rootResolvers = new ArrayList<>();
    private final Map<Class<?>, List<BeanDefinitionAndMethods>> typeResolvers = new HashMap<>();

    public void registerRootResolverExecutableMethod(BeanDefinition<?> beanDefinition, ExecutableMethod<Object, ?> method) {
        rootResolvers.stream()
                .filter(it -> it.getBeanDefinition().equals(beanDefinition))
                .findFirst()
                .orElseGet(() -> {
                    BeanDefinitionAndMethods item = new BeanDefinitionAndMethods(beanDefinition);
                    rootResolvers.add(item);
                    return item;
                })
                .addExecutableMethod(method);
    }

    public void registerTypeResolverExecutableMethod(BeanDefinition<?> beanDefinition, ExecutableMethod<Object, ?> method) {
        AnnotationClassValue<Class<?>> annotationValue = (AnnotationClassValue<Class<?>>) beanDefinition
                .getValue(GraphQLTypeResolver.class, AnnotationMetadata.VALUE_MEMBER)
                .orElseThrow(() -> new IncorrectAnnotationException(beanDefinition.getBeanType()));

        Class<?> modelClass = annotationValue.getType().get();

        typeResolvers.putIfAbsent(modelClass, new ArrayList<>());

        typeResolvers.get(modelClass).stream()
                .filter(it -> it.getBeanDefinition().equals(beanDefinition))
                .findFirst()
                .orElseGet(() -> {
                    BeanDefinitionAndMethods item = new BeanDefinitionAndMethods(beanDefinition);
                    typeResolvers.get(modelClass).add(item);
                    return item;
                })
                .addExecutableMethod(method);
    }

    BeanDefinitionAndMethod getRootExecutableMethod(@NonNull String methodName) {
        for (BeanDefinitionAndMethods item : rootResolvers) {
            for (ExecutableMethod<Object, ?> executableMethod : item.getExecutableMethods()) {
                if (executableMethod.getMethodName().equals(methodName)) {
                    return new BeanDefinitionAndMethod(item.getBeanDefinition(), executableMethod);
                }
            }
        }
        throw new MethodNotFoundException(methodName);
    }

    // TODO throw an exception
    Optional<BeanDefinitionAndMethod> getTypeExecutableMethod(@NonNull Class<?> beanType,
                                                              @NonNull String methodName) {
        List<BeanDefinitionAndMethods> items = typeResolvers.get(beanType);

        if (items == null) {
            return Optional.empty();
        }

        for (BeanDefinitionAndMethods item : items) {
            for (ExecutableMethod<Object, ?> executableMethod : item.getExecutableMethods()) {
                if (executableMethod.getMethodName().equals(methodName)) {
                    return Optional.of(new BeanDefinitionAndMethod(item.getBeanDefinition(), executableMethod));
                }
            }
        }

        return Optional.empty();
    }

}
