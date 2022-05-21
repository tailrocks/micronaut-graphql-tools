package io.micronaut.graphql.tools.exceptions;

public abstract class AbstractMappingException extends RuntimeException {

    private final String graphQlType;
    private final String graphQlField;
    private final Class mappedClass;
    private final String mappedMethod;

    public AbstractMappingException(
            String message,
            String graphQlType,
            String graphQlField,
            Class mappedClass,
            String mappedMethod
    ) {
        super(message);
        this.graphQlType = graphQlType;
        this.graphQlField = graphQlField;
        this.mappedClass = mappedClass;
        this.mappedMethod = mappedMethod;
    }

    public String getGraphQlField() {
        return graphQlField;
    }

    public String getGraphQlType() {
        return graphQlType;
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

        builder.append("\n  GraphQL type: ").append(graphQlType);
        builder.append("\n  GraphQL field: ").append(graphQlField);
        builder.append("\n  Mapped class: ").append(mappedClass.getName());
        builder.append("\n  Mapped method: ").append(mappedMethod);

        return builder.toString();
    }

}
