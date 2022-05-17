package io.micronaut.graphql.tools.exceptions;

public abstract class AbstractMappingException extends RuntimeException {

    private final String graphQlType;
    private final String graphQlField;
    private final Class mappedClass;
    private final String mappedMethod;
    private final Class providedClass;

    public AbstractMappingException(
            String message,
            String graphQlType,
            String graphQlField,
            Class mappedClass,
            String mappedMethod,
            Class providedClass
    ) {
        super(message);
        this.graphQlType = graphQlType;
        this.graphQlField = graphQlField;
        this.mappedClass = mappedClass;
        this.mappedMethod = mappedMethod;
        // TODO move away from here
        this.providedClass = providedClass;
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

    // TODO move away from here
    public Class getProvidedClass() {
        return providedClass;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());

        builder.append("\n  GraphQL type: ").append(graphQlType);
        builder.append("\n  GraphQL field: ").append(graphQlField);
        builder.append("\n  Mapped class: ").append(mappedClass.getName());
        builder.append("\n  Mapped method: ").append(mappedMethod);

        // TODO move away from here
        if (providedClass != null) {
            builder.append("\n  Provided class: ").append(providedClass.getName());
        }

        return builder.toString();
    }

}
