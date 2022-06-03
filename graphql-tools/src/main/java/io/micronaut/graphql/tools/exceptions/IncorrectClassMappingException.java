package io.micronaut.graphql.tools.exceptions;

import io.micronaut.core.annotation.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

public class IncorrectClassMappingException extends AbstractMappingMethodException {

    private final Class providedClass;
    private final Set<Class> supportedClasses;

    public static IncorrectClassMappingException ofCustomTypeMappedToBuiltInClass(
            MappingDetails mappingDetails,
            Class providedClass
    ) {
        return new IncorrectClassMappingException(
                "The field is mapped to the built-in class, when required custom Java class.",
                mappingDetails, providedClass, null
        );
    }

    public static IncorrectClassMappingException ofBuiltInTypeMappedToCustomClass(
            MappingDetails mappingDetails,
            Class providedClass,
            Set<Class> supportedClasses
    ) {
        return new IncorrectClassMappingException(
                "The field is mapped to the incorrect class.",
                mappingDetails, providedClass, supportedClasses
        );
    }

    public static IncorrectClassMappingException ofEnumMappedToNotEnum(
            MappingDetails mappingDetails,
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
                mappingDetails, providedClass, null
        );
    }

    public IncorrectClassMappingException(String message, MappingDetails mappingDetails, Class providedClass,
                                          @Nullable Set<Class> supportedClasses) {
        super(message, mappingDetails);

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
