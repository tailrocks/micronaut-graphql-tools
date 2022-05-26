package io.micronaut.graphql.tools;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.graphql.tools.annotation.GraphQLInput;
import io.micronaut.graphql.tools.annotation.GraphQLType;
import io.micronaut.graphql.tools.exceptions.ClassNotIntrospectedException;
import io.micronaut.graphql.tools.exceptions.ImplementationNotFoundException;
import io.micronaut.graphql.tools.exceptions.IncorrectImplementationException;
import io.micronaut.graphql.tools.exceptions.MultipleImplementationsFoundException;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class GraphQLBeanIntrospectionRegistry {

    private static final Map<Class, BeanIntrospection> TYPE_INTROSPECTIONS = new HashMap<>();
    private static final Map<Class, BeanIntrospection> INPUT_INTROSPECTIONS = new HashMap<>();

    // key: key interface
    // value: implemented classes
    private static final Map<Class, List<Class>> INTERFACE_TO_IMPLEMENTATION = new HashMap<>();

    // key: key interface
    // value: implemented classes
    private static final Map<Class, Class> IMPLEMENTATION_TO_INTERFACE = new HashMap<>();

    private static boolean TYPE_INTROSPECTIONS_LOADED = false;
    private static boolean INPUT_INTROSPECTIONS_LOADED = false;

    public GraphQLBeanIntrospectionRegistry() {
    }

    private static void loadTypeIntrospections() {
        if (TYPE_INTROSPECTIONS_LOADED) {
            return;
        }

        TYPE_INTROSPECTIONS.clear();
        INTERFACE_TO_IMPLEMENTATION.clear();

        for (BeanIntrospection<Object> introspection : BeanIntrospector.SHARED.findIntrospections(GraphQLType.class)) {
            TYPE_INTROSPECTIONS.put(introspection.getBeanType(), introspection);

            AnnotationValue<GraphQLType> annotationValue = introspection.getAnnotation(GraphQLType.class);
            Class modelInterface = annotationValue.get(AnnotationMetadata.VALUE_MEMBER, Class.class).get();

            if (modelInterface.isInterface()) {
                INTERFACE_TO_IMPLEMENTATION.putIfAbsent(modelInterface, new ArrayList<>());
                INTERFACE_TO_IMPLEMENTATION.get(modelInterface).add(introspection.getBeanType());

                IMPLEMENTATION_TO_INTERFACE.put(introspection.getBeanType(), modelInterface);
            }
        }

        TYPE_INTROSPECTIONS_LOADED = true;
    }

    private static void loadInputIntrospections() {
        if (INPUT_INTROSPECTIONS_LOADED) {
            return;
        }

        INPUT_INTROSPECTIONS.clear();

        for (BeanIntrospection<Object> introspection : BeanIntrospector.SHARED.findIntrospections(GraphQLInput.class)) {
            // TODO remove it from here
            if (
                    introspection.getBeanType().isInterface()
                            || introspection.getBeanType().isEnum()
                            || introspection.getBeanType().isAnnotation()
            ) {
                throw new RuntimeException(GraphQLInput.class.getSimpleName() + " annotation can be used only on classes. Found wrong usage: " + introspection.getBeanType());
            }
            INPUT_INTROSPECTIONS.put(introspection.getBeanType(), introspection);
        }

        INPUT_INTROSPECTIONS_LOADED = true;
    }

    public BeanIntrospection getGraphQlTypeBeanIntrospection(
            Class<?> clazz,
            FieldDefinition fieldDefinition,
            ObjectTypeDefinition objectTypeDefinition,
            Class<?> mappedClass,
            String mappedMethodName
    ) {
        loadTypeIntrospections();

        if (clazz.isInterface()) {
            if (!INTERFACE_TO_IMPLEMENTATION.containsKey(clazz)) {
                throw new ImplementationNotFoundException(
                        objectTypeDefinition.getName(),
                        fieldDefinition.getName(),
                        mappedClass,
                        mappedMethodName,
                        clazz
                );
            }

            if (INTERFACE_TO_IMPLEMENTATION.get(clazz).size() > 1) {
                throw new MultipleImplementationsFoundException(
                        objectTypeDefinition.getName(),
                        fieldDefinition.getName(),
                        mappedClass,
                        mappedMethodName,
                        clazz,
                        INTERFACE_TO_IMPLEMENTATION.get(clazz)
                );
            }

            Class<?> implementationClass = INTERFACE_TO_IMPLEMENTATION.get(clazz).get(0);

            if (!clazz.isAssignableFrom(implementationClass)) {
                throw new IncorrectImplementationException(
                        objectTypeDefinition.getName(),
                        fieldDefinition.getName(),
                        mappedClass,
                        mappedMethodName,
                        clazz,
                        implementationClass
                );
            }

            return TYPE_INTROSPECTIONS.get(implementationClass);
        } else {
            if (!TYPE_INTROSPECTIONS.containsKey(clazz)) {
                throw new ClassNotIntrospectedException(
                        objectTypeDefinition.getName(),
                        fieldDefinition.getName(),
                        mappedClass,
                        mappedMethodName,
                        clazz
                );
            }

            return TYPE_INTROSPECTIONS.get(clazz);
        }
    }

    // get interface by class implementation or return the current class if it's not implement any interface
    public Class getInterfaceClass(Class implementationClass) {
        loadTypeIntrospections();

        return IMPLEMENTATION_TO_INTERFACE.getOrDefault(implementationClass, implementationClass);
    }

}
