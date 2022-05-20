package io.micronaut.graphql.tools.exceptions;

public class CustomTypeMappedToBuiltInClassException extends AbstractMappingException {

    public CustomTypeMappedToBuiltInClassException(String graphQlTypeName, String graphQlFieldName, Class mappedClass,
                                                   String mappedMethodName, Class providedClass) {
        super(
                "The field is mapped to the built-in class, but required custom Java class.",
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethodName, providedClass
        );
    }

}
