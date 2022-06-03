package io.micronaut.graphql.tools.exceptions;

public class IncorrectImplementationException extends AbstractMappingMethodException {

    private final Class implementationClass;

    public IncorrectImplementationException(MappingDetails mappingDetails, Class interfaceClass, Class implementationClass) {
        super(
                String.format(
                        "The annotated implementation class is not implementing the %s interface.",
                        interfaceClass.getName()
                ),
                mappingDetails
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
