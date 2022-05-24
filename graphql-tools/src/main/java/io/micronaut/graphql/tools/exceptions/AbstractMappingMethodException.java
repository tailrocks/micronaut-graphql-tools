package io.micronaut.graphql.tools.exceptions;

public abstract class AbstractMappingMethodException extends AbstractMappingException {

    private final Class mappedClass;
    private final String mappedMethod;

    public AbstractMappingMethodException(String message, String graphQlType, String graphQlField, Class mappedClass,
                                          String mappedMethod) {
        super(message, graphQlType, graphQlField);

        this.mappedClass = mappedClass;
        this.mappedMethod = mappedMethod;
    }

    public Class getMappedClass() {
        return mappedClass;
    }

    public String getMappedMethod() {
        return mappedMethod;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());

        builder.append("\n  Mapped class: ").append(mappedClass.getName());
        builder.append("\n  Mapped method: ").append(mappedMethod);

        return builder.toString();
    }

}
