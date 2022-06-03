package io.micronaut.graphql.tools.exceptions;

import io.micronaut.graphql.tools.annotation.GraphQLType;

public class ClassNotIntrospectedException extends AbstractMappingMethodException {

    public ClassNotIntrospectedException(MappingDetails mappingDetails, Class returningType) {
        super(
                String.format(
                        "The class %s is not introspected. Ensure the class is annotated with %s.",
                        returningType.getName(),
                        GraphQLType.class.getName()
                ),
                mappingDetails
        );
    }

}
