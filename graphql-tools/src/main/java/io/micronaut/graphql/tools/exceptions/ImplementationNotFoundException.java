package io.micronaut.graphql.tools.exceptions;

public class ImplementationNotFoundException extends AbstractMappingMethodException {

    public ImplementationNotFoundException(String graphQlTypeName, String graphQlFieldName, Class mappedClass,
                                           String mappedMethod, Class interfaceClass) {
        super(
                String.format(
                        "Can not find implementation class for the interface %s.",
                        interfaceClass.getName()
                ),
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethod
        );
    }

}
