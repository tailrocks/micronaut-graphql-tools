package io.micronaut.graphql.tools.exceptions;

public class ClassNotIntrospectedException extends AbstractMappingMethodException {

    public ClassNotIntrospectedException(MappingDetails mappingDetails, Class returningType, Class annotationClass) {
        super(
                String.format(
                        "The class %s is not introspected. Ensure the class is annotated with %s.",
                        returningType.getName(),
                        annotationClass.getName()
                ),
                mappingDetails
        );
    }

}
