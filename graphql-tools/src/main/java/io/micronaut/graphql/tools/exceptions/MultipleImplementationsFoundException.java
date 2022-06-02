package io.micronaut.graphql.tools.exceptions;

import java.util.List;
import java.util.stream.Collectors;

public class MultipleImplementationsFoundException extends AbstractMappingMethodException {

    private final List<Class> implementationClasses;

    public MultipleImplementationsFoundException(String graphQlTypeName, String graphQlFieldName, Class mappedClass,
                                                 String mappedMethod, Class interfaceClass,
                                                 List<Class> implementationClasses) {
        super(
                String.format(
                        "Found multiple implementations for the interface %s.",
                        interfaceClass.getName()
                ),
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethod
        );

        this.implementationClasses = implementationClasses;
    }

    public List<Class> getImplementationClasses() {
        return implementationClasses;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());

        builder.append("\n  Implementation classes: ").append(
                implementationClasses.stream()
                        .map(Class::getName)
                        .collect(Collectors.joining(", "))
        );

        return builder.toString();
    }

}