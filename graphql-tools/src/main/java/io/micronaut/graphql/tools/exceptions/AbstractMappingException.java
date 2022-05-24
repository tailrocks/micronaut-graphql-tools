package io.micronaut.graphql.tools.exceptions;

public abstract class AbstractMappingException extends RuntimeException {

    private final String graphQlType;
    private final String graphQlField;

    public AbstractMappingException(
            String message,
            String graphQlType,
            String graphQlField
    ) {
        super(message);
        this.graphQlType = graphQlType;
        this.graphQlField = graphQlField;
    }

    public String getGraphQlField() {
        return graphQlField;
    }

    public String getGraphQlType() {
        return graphQlType;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());

        builder.append("\n  GraphQL type: ").append(graphQlType);
        builder.append("\n  GraphQL field: ").append(graphQlField);

        return builder.toString();
    }

}
