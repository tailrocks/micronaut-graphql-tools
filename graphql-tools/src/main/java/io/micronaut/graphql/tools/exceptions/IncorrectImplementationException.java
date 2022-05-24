package io.micronaut.graphql.tools.exceptions;

public class IncorrectImplementationException extends AbstractMappingMethodException {

    private final Class implementationClass;

    public IncorrectImplementationException(String graphQlTypeName, String graphQlFieldName, Class mappedClass,
                                            String mappedMethod, Class interfaceClass, Class implementationClass) {
        super(
                String.format(
                        "The annotated implementation class is not implementing the %s interface.",
                        interfaceClass.getName()
                ),
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethod
        );

        this.implementationClass = implementationClass;
    }

    public Class getImplementationClass() {
        return implementationClass;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());

        builder.append("\n  Implementation class: ").append(implementationClass.getName());

        return builder.toString();
    }

}
