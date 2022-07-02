package io.micronaut.graphql.tools.exceptions;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MultipleImplementationsFoundException extends AbstractMappingMethodException {

    private final List<Class<?>> implementationClasses;

    public MultipleImplementationsFoundException(MappingDetails mappingDetails, Class<?> interfaceClass,
                                                 List<Class<?>> implementationClasses) {
        super(
                String.format(
                        "Found multiple implementations for the interface %s.",
                        interfaceClass.getName()
                ),
                mappingDetails
        );

        this.implementationClasses = implementationClasses.stream()
                .sorted(Comparator.comparing(Class::getName))
                .collect(Collectors.toList());
    }

    public List<Class<?>> getImplementationClasses() {
        return implementationClasses;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "\n  Implementation classes: " +
                implementationClasses.stream()
                        .map(Class::getName)
                        .collect(Collectors.joining(", "));
    }

}
