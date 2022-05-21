package io.micronaut.graphql.tools.exceptions;

public class InvalidSourceArgumentException extends AbstractMappingException {

    private final Class providedClass;
    private final Class requiredClass;

    public InvalidSourceArgumentException(String graphQlTypeName, String graphQlFieldName,
                                          Class mappedClass, String mappedMethod, Class providedClass,
                                          Class requiredClass) {
        super(
                String.format(
                        "The source argument must be instance of %s class, provided: %s.",
                        requiredClass.getName(),
                        providedClass.getName()
                ),
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethod
        );

        this.providedClass = providedClass;
        this.requiredClass = requiredClass;
    }

    public Class getProvidedClass() {
        return providedClass;
    }

    public Class getRequiredClass() {
        return requiredClass;
    }

}
