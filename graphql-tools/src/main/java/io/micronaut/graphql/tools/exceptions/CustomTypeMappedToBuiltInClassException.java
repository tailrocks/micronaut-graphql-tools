package io.micronaut.graphql.tools.exceptions;

public class CustomTypeMappedToBuiltInClassException extends AbstractMappingMethodException {

    private final Class providedClass;

    public CustomTypeMappedToBuiltInClassException(String graphQlTypeName, String graphQlFieldName, Class mappedClass,
                                                   String mappedMethod, Class providedClass) {
        super(
                "The field is mapped to the built-in class, but required custom Java class.",
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethod
        );

        this.providedClass = providedClass;
    }

    public Class getProvidedClass() {
        return providedClass;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());

        builder.append("\n  Provided class: ").append(providedClass.getName());

        return builder.toString();
    }

}
