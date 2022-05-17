package io.micronaut.graphql.tools.exceptions;

public class CustomTypeMappedToBuiltInClassException extends AbstractMappingException {

    public CustomTypeMappedToBuiltInClassException(String graphQlTypeName, String graphQlFieldName, Class mappedClass,
                                                   String mappedMethodName, Class providedClass) {
        super(
                String.format(
                        "The field `%s` is mapped to built-in class %s, but required custom Java class",
                        graphQlFieldName,
                        providedClass
                ),
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethodName, providedClass
        );
    }

}
