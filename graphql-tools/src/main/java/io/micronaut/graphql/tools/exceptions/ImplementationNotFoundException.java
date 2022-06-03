package io.micronaut.graphql.tools.exceptions;

public class ImplementationNotFoundException extends AbstractMappingMethodException {

    public ImplementationNotFoundException(MappingDetails mappingDetails, Class interfaceClass) {
        super(
                String.format(
                        "Can not find implementation class for the interface %s.",
                        interfaceClass.getName()
                ),
                mappingDetails
        );
    }

}
