package io.micronaut.graphql.tools.exceptions;

import java.util.Set;
import java.util.stream.Collectors;

public class IncorrectBuiltInScalarMappingException extends RuntimeException {

    private final String graphQlTypeName;
    private final Class providedClass;
    private final Set<Class> supportedClasses;

    private String methodName;
    private Integer position;

    public IncorrectBuiltInScalarMappingException(String graphQlTypeName, Class providedClass, Set<Class> supportedClasses) {
        super(String.format(
                "The type `%s` is mapped to incorrect class %s, supported classes: %s",
                graphQlTypeName,
                providedClass,
                supportedClasses.stream()
                        .map(Class::getName)
                        .collect(Collectors.joining(", "))
        ));
        this.graphQlTypeName = graphQlTypeName;
        this.providedClass = providedClass;
        this.supportedClasses = supportedClasses;
    }

    public String getGraphQlTypeName() {
        return graphQlTypeName;
    }

    public Class getProvidedClass() {
        return providedClass;
    }

    public Set<Class> getSupportedClasses() {
        return supportedClasses;
    }

    public String getMethodName() {
        return methodName;
    }

    public Integer getPosition() {
        return position;
    }

    public IncorrectBuiltInScalarMappingException withMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public IncorrectBuiltInScalarMappingException withPosition(int position) {
        this.position = position;
        return this;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());

        if (methodName != null) {
            builder.append("\n  Method Name: ").append(methodName);
        }

        if (position != null) {
            builder.append("\n  Position: ").append(position);
        }

        return builder.toString();
    }

}
