package io.micronaut.graphql.tools;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.graphql.tools.annotation.GraphQLType;
import io.micronaut.graphql.tools.exceptions.ClassNotIntrospectedException;
import io.micronaut.graphql.tools.exceptions.ImplementationNotFoundException;
import io.micronaut.graphql.tools.exceptions.IncorrectImplementationException;
import io.micronaut.graphql.tools.exceptions.MappingDetails;
import io.micronaut.graphql.tools.exceptions.MultipleImplementationsFoundException;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class GraphQLBeanIntrospectionRegistry {

    private final Map<Class, BeanIntrospection> typeIntrospections = new HashMap<>();

    // key: key interface
    // value: implemented classes
    private final Map<Class, List<Class>> interfaceToImplementation = new HashMap<>();

    // key: key interface
    // value: implemented classes
    private final Map<Class, Class> implementationToInterface = new HashMap<>();

    private boolean typeIntrospectionsLoaded = false;

    public GraphQLBeanIntrospectionRegistry() {
    }

    private void loadTypeIntrospections() {
        if (typeIntrospectionsLoaded) {
            return;
        }

        typeIntrospections.clear();
        interfaceToImplementation.clear();

        for (BeanIntrospection<Object> introspection : BeanIntrospector.SHARED.findIntrospections(GraphQLType.class)) {
            typeIntrospections.put(introspection.getBeanType(), introspection);

            AnnotationValue<GraphQLType> annotationValue = introspection.getAnnotation(GraphQLType.class);
            Class modelInterface = annotationValue.get(AnnotationMetadata.VALUE_MEMBER, Class.class).get();

            if (modelInterface.isInterface()) {
                interfaceToImplementation.putIfAbsent(modelInterface, new ArrayList<>());
                interfaceToImplementation.get(modelInterface).add(introspection.getBeanType());

                implementationToInterface.put(introspection.getBeanType(), modelInterface);
            }
        }

        typeIntrospectionsLoaded = true;
    }

    public BeanIntrospection getGraphQlTypeBeanIntrospection(
            MappingDetails mappingDetails,
            Class<?> clazz
    ) {
        loadTypeIntrospections();

        if (clazz.isInterface()) {
            if (!interfaceToImplementation.containsKey(clazz)) {
                throw new ImplementationNotFoundException(mappingDetails, clazz);
            }

            if (interfaceToImplementation.get(clazz).size() > 1) {
                throw new MultipleImplementationsFoundException(
                        mappingDetails,
                        clazz,
                        interfaceToImplementation.get(clazz)
                );
            }

            Class<?> implementationClass = interfaceToImplementation.get(clazz).get(0);

            if (!clazz.isAssignableFrom(implementationClass)) {
                throw new IncorrectImplementationException(mappingDetails, clazz, implementationClass);
            }

            return typeIntrospections.get(implementationClass);
        } else {
            if (!typeIntrospections.containsKey(clazz)) {
                throw new ClassNotIntrospectedException(mappingDetails, clazz, GraphQLType.class);
            }

            return typeIntrospections.get(clazz);
        }
    }

    // get interface by class implementation or return the current class if it's not implement any interface
    public Class getInterfaceClass(Class implementationClass) {
        loadTypeIntrospections();

        return implementationToInterface.getOrDefault(implementationClass, implementationClass);
    }

}
