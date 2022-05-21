package io.micronaut.graphql.tools.exceptions;

import java.util.Set;
import java.util.stream.Collectors;

public class IncorrectBuiltInScalarMappingException extends AbstractMappingException {

    private final Class providedClass;
    private final Set<Class> supportedClasses;

    public IncorrectBuiltInScalarMappingException(String graphQlTypeName, String graphQlFieldName, Class mappedClass,
                                                  String mappedMethodName, Class providedClass,
                                                  Set<Class> supportedClasses) {
        super(
                "The field is mapped to the incorrect class.",
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethodName
        );

        this.providedClass = providedClass;
        this.supportedClasses = supportedClasses;
    }

    public Class getProvidedClass() {
        return providedClass;
    }

    public Set<Class> getSupportedClasses() {
        return supportedClasses;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());

        builder.append("\n  Provided class: ").append(providedClass.getName());
        builder.append("\n  Supported classes: ").append(
                supportedClasses.stream()
                        .map(Class::getName)
                        .collect(Collectors.joining(", "))
        );

        return builder.toString();
    }

}
