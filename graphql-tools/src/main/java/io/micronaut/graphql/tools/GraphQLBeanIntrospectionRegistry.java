/*
 * Copyright 2021-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private final Map<Class<?>, BeanIntrospection<Object>> typeIntrospections = new HashMap<>();

    // key: key interface
    // value: implemented classes
    private final Map<Class<?>, List<Class<?>>> interfaceToImplementation = new HashMap<>();

    // key: key interface
    // value: implemented classes
    private final Map<Class<?>, Class<?>> implementationToInterface = new HashMap<>();

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
            Class<?> modelInterface = annotationValue.get(AnnotationMetadata.VALUE_MEMBER, Class.class).get();

            if (modelInterface.isInterface()) {
                interfaceToImplementation.putIfAbsent(modelInterface, new ArrayList<>());
                interfaceToImplementation.get(modelInterface).add(introspection.getBeanType());

                implementationToInterface.put(introspection.getBeanType(), modelInterface);
            }
        }

        typeIntrospectionsLoaded = true;
    }

    public BeanIntrospection<Object> getGraphQlTypeBeanIntrospection(
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

    /**
     * Get interface by class implementation or return the current class if it's not implement any interfaces.
     *
     * @param implementationClass the implementation class
     * @return original class or interface class
     */
    public Class getInterfaceClass(Class implementationClass) {
        loadTypeIntrospections();

        return implementationToInterface.getOrDefault(implementationClass, implementationClass);
    }

}
