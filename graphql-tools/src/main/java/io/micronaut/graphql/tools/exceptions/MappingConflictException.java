package io.micronaut.graphql.tools.exceptions;

public class MappingConflictException extends AbstractMappingMethodException {

    public MappingConflictException(MappingDetails mappingDetails, String graphQlType,
                                    String conflictedGraphQlTypeName, Class providedClass, Class registeredClass) {
        super(
                String.format(
                        "Unable to map GraphQL %s `%s` to %s, as it is already mapped to %s.",
                        graphQlType,
                        conflictedGraphQlTypeName,
                        providedClass.getName(),
                        registeredClass.getName()
                ),
                mappingDetails
        );
    }

}
