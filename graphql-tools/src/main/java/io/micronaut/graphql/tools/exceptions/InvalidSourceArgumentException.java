package io.micronaut.graphql.tools.exceptions;

public class InvalidSourceArgumentException extends AbstractMappingMethodException {

    private final Class providedClass;
    private final Class requiredClass;

    public InvalidSourceArgumentException(MappingDetails mappingDetails, Class providedClass,
                                          Class requiredClass) {
        super(
                String.format(
                        "The source argument must be instance of %s class, provided: %s.",
                        requiredClass.getName(),
                        providedClass.getName()
                ),
                mappingDetails
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
