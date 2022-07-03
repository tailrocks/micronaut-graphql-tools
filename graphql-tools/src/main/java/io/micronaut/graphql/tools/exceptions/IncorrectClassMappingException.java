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
package io.micronaut.graphql.tools.exceptions;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.graphql.tools.MappingContext;
import io.micronaut.graphql.tools.SystemTypes;

import java.util.Set;
import java.util.stream.Collectors;

public class IncorrectClassMappingException extends AbstractMappingException {

    public enum MappingType {
        DETECT_TYPE,
        BUILT_IN_JAVA_CLASS,
        CUSTOM_CLASS,
        ENUM,
        INTERFACE
    }

    private final Class<?> providedClass;
    private final Set<Class<?>> supportedClasses;

    public static IncorrectClassMappingException forField(
            MappingType providedType,
            MappingType requiredType,
            MappingContext mappingContext,
            Class<?> providedClass,
            @Nullable Set<Class<?>> supportedClasses
    ) {
        return new IncorrectClassMappingException(
                String.format(
                        "The field is mapped to %s, when required %s.",
                        toString(providedType, providedClass),
                        toString(requiredType, null)
                ),
                mappingContext,
                providedClass,
                supportedClasses
        );
    }

    public static IncorrectClassMappingException forArgument(
            MappingType providedType,
            MappingType requiredType,
            MappingContext mappingContext,
            Class<?> providedClass,
            @Nullable Set<Class<?>> supportedClasses
    ) {
        return new IncorrectClassMappingException(
                String.format(
                        "The argument is mapped to %s, when required %s.",
                        toString(providedType, providedClass),
                        toString(requiredType, null)
                ),
                mappingContext,
                providedClass,
                supportedClasses
        );
    }

    public IncorrectClassMappingException(String message, MappingContext mappingContext, Class<?> providedClass,
                                          @Nullable Set<Class<?>> supportedClasses) {
        super(message, mappingContext);

        this.providedClass = providedClass;
        this.supportedClasses = supportedClasses;
    }

    public Class<?> getProvidedClass() {
        return providedClass;
    }

    @Nullable
    public Set<Class<?>> getSupportedClasses() {
        return supportedClasses;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());

        builder.append("\n  Provided class: ").append(providedClass.getName());
        if (supportedClasses != null) {
            builder.append("\n  Supported classes: ").append(
                    supportedClasses.stream()
                            .map(Class::getName)
                            .sorted()
                            .collect(Collectors.joining(", "))
            );
        }

        return builder.toString();
    }

    private static String toString(MappingType mappingType, Class<?> providedClass) {
        switch (mappingType) {
            case BUILT_IN_JAVA_CLASS:
                return "a built-in class";
            case CUSTOM_CLASS:
                return "a custom class";
            case ENUM:
                return "an enum";
            case INTERFACE:
                return "an interface";
            case DETECT_TYPE:
                return toString(providedClass);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static String toString(Class<?> clazz) {
        if (clazz.isAnnotation()) {
            return "an annotation";
        }
        if (clazz.isInterface()) {
            return "an interface";
        }
        if (clazz.isPrimitive()) {
            return "a primitive";
        }
        if (clazz.isEnum()) {
            return "an enum";
        }
        if (SystemTypes.isJavaBuiltInClass(clazz)) {
            return "a built-in class";
        }
        return "a custom class";
    }

}
