package io.micronaut.graphql.tools.exceptions;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.graphql.tools.SystemTypes;

import java.util.Set;
import java.util.stream.Collectors;

public class IncorrectClassMappingException extends AbstractMappingMethodException {

    public enum MappingType {
        DETECT_TYPE,
        BUILT_IN_JAVA_CLASS,
        CUSTOM_JAVA_CLASS,
        ENUM
    }

    private final Class<?> providedClass;
    private final Set<Class<?>> supportedClasses;

    public static IncorrectClassMappingException forField(
            MappingType providedType,
            MappingType requiredType,
            MappingDetails mappingDetails,
            Class<?> providedClass,
            @Nullable Set<Class<?>> supportedClasses
    ) {
        return new IncorrectClassMappingException(
                String.format(
                        "The field is mapped to %s, when required %s.",
                        toString(providedType, providedClass),
                        toString(requiredType, null)
                ),
                mappingDetails,
                providedClass,
                supportedClasses
        );
    }

    public static IncorrectClassMappingException forArgument(
            MappingType providedType,
            MappingType requiredType,
            MappingDetails mappingDetails,
            Class<?> providedClass,
            @Nullable Set<Class<?>> supportedClasses
    ) {
        return new IncorrectClassMappingException(
                String.format(
                        "The argument is mapped to %s, when required %s.",
                        toString(providedType, providedClass),
                        toString(requiredType, null)
                ),
                mappingDetails,
                providedClass,
                supportedClasses
        );
    }

    public IncorrectClassMappingException(String message, MappingDetails mappingDetails, Class<?> providedClass,
                                          @Nullable Set<Class<?>> supportedClasses) {
        super(message, mappingDetails);

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

    private static String toString(MappingType mappingType, Class providedClass) {
        switch (mappingType) {
            case BUILT_IN_JAVA_CLASS:
                return "built-in class";
            case CUSTOM_JAVA_CLASS:
                return "custom Java class";
            case ENUM:
                return "enum";
            case DETECT_TYPE:
                return toString(providedClass);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static String toString(Class<?> clazz) {
        if (clazz.isAnnotation()) {
            return "annotation";
        }
        if (clazz.isInterface()) {
            return "interface";
        }
        if (clazz.isPrimitive()) {
            return "primitive";
        }
        if (clazz.isEnum()) {
            return "enum";
        }
        if (SystemTypes.isJavaBuiltInClass(clazz)) {
            return "built-in class";
        }
        return "custom Java class";
    }

}
