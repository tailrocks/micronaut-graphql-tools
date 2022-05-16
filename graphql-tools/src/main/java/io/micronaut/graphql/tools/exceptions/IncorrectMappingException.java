package io.micronaut.graphql.tools.exceptions;

public class IncorrectMappingException extends RuntimeException {

    private final String graphQlTypeName;
    private final String graphQlFieldName;
    private final Class mappedClass;
    private final String mappedMethodName;
    private final Class providedClass;

    public IncorrectMappingException(
            String graphQlTypeName,
            String graphQlFieldName,
            Class mappedClass,
            String mappedMethodName,
            Class providedClass
    ) {
        super(String.format(
                "The field `%s` is mapped to built-in class %s, but required custom Java class",
                graphQlFieldName,
                providedClass
        ));
        this.graphQlTypeName = graphQlTypeName;
        this.graphQlFieldName = graphQlFieldName;
        this.mappedClass = mappedClass;
        this.mappedMethodName = mappedMethodName;
        this.providedClass = providedClass;
    }

    public String getGraphQlFieldName() {
        return graphQlFieldName;
    }

    public String getGraphQlTypeName() {
        return graphQlTypeName;
    }

    public Class getMappedClass() {
        return mappedClass;
    }

    public String getMappedMethodName() {
        return mappedMethodName;
    }

    public Class getProvidedClass() {
        return providedClass;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());

        builder.append("\n  GraphQL type: ").append(graphQlTypeName);
        builder.append("\n  GraphQL field: ").append(graphQlFieldName);
        builder.append("\n  Mapped class: ").append(mappedClass.getName());
        builder.append("\n  Mapped method name: ").append(mappedMethodName);
        builder.append("\n  Provided class: ").append(providedClass.getName());

        return builder.toString();
    }

}
