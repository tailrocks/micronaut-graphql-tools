package io.micronaut.graphql.tools.exceptions;

import io.micronaut.core.annotation.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

public class IncorrectClassMappingException extends AbstractMappingMethodException {

    private final Class providedClass;
    private final Set<Class> supportedClasses;

    public static IncorrectClassMappingException ofCustomTypeMappedToBuiltInClass(
            String graphQlTypeName, String graphQlFieldName, Class mappedClass, String mappedMethod,
            Class providedClass
    ) {
        return new IncorrectClassMappingException(
                "The field is mapped to the built-in class, when required custom Java class.",
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethod, providedClass, null
        );
    }

    public static IncorrectClassMappingException ofBuiltInTypeMappedToCustomClass(
            String graphQlTypeName, String graphQlFieldName, Class mappedClass, String mappedMethod,
            Class providedClass, Set<Class> supportedClasses
    ) {
        return new IncorrectClassMappingException(
                "The field is mapped to the incorrect class.",
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethod, providedClass, supportedClasses
        );
    }

    public static IncorrectClassMappingException ofEnumMappedToNotEnum(
            String graphQlTypeName, String graphQlFieldName, Class mappedClass, String mappedMethod,
            Class providedClass
    ) {
        String type = "class";
        if (providedClass.isAnnotation()) {
            type = "annotation";
        }
        if (providedClass.isInterface()) {
            type = "interface";
        }
        if (providedClass.isPrimitive()) {
            type = "primitive";
        }

        return new IncorrectClassMappingException(
                "The field is mapped to the " + type + ", when required Enum.",
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethod, providedClass, null
        );
    }

    public IncorrectClassMappingException(String message, String graphQlTypeName, String graphQlFieldName,
                                          Class mappedClass, String mappedMethod, Class providedClass,
                                          @Nullable Set<Class> supportedClasses) {
        super(message, graphQlTypeName, graphQlFieldName, mappedClass, mappedMethod);

        this.providedClass = providedClass;
        this.supportedClasses = supportedClasses;
    }

    public Class getProvidedClass() {
        return providedClass;
    }

    @Nullable
    public Set<Class> getSupportedClasses() {
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

}
