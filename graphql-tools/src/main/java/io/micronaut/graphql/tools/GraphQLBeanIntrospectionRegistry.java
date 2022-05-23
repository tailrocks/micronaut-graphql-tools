package io.micronaut.graphql.tools;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.graphql.tools.annotation.GraphQLInput;
import io.micronaut.graphql.tools.annotation.GraphQLType;
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

    public GraphQLBeanIntrospectionRegistry() {
    }

    private static void loadTypeIntrospections() {
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
    }

    private static void loadInputIntrospections() {
        INPUT_INTROSPECTIONS.clear();

        for (BeanIntrospection<Object> introspection : BeanIntrospector.SHARED.findIntrospections(GraphQLInput.class)) {
            if (
                    introspection.getBeanType().isInterface()
                            || introspection.getBeanType().isEnum()
                            || introspection.getBeanType().isAnnotation()
            ) {
                throw new RuntimeException(GraphQLInput.class.getSimpleName() + " annotation can be used only on classes. Found wrong usage: " + introspection.getBeanType());
            }
            INPUT_INTROSPECTIONS.put(introspection.getBeanType(), introspection);
        }
    }

    public BeanIntrospection requireGraphQLModel(Class clazz) {
        loadTypeIntrospections();

        if (clazz.isInterface()) {
            if (!INTERFACE_TO_IMPLEMENTATION.containsKey(clazz)) {
                throw new RuntimeException("Can not find implementation class for: " + clazz);
            }

            if (INTERFACE_TO_IMPLEMENTATION.get(clazz).size() > 1) {
                throw new RuntimeException("Found multiple implementations for: " + clazz);
            }

            Class implementationClass = INTERFACE_TO_IMPLEMENTATION.get(clazz).get(0);

            if (!clazz.isAssignableFrom(implementationClass)) {
                throw new RuntimeException(implementationClass + " is not implementing " + clazz);
            }

            return TYPE_INTROSPECTIONS.get(implementationClass);
        } else {
            if (!TYPE_INTROSPECTIONS.containsKey(clazz)) {
                throw new RuntimeException(clazz + " is not introspected. Probably " + GraphQLType.class.getSimpleName() + " annotation was not added or processed by Micronaut.");
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
