package io.micronaut.graphql.tools.exceptions;

import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver;

public class IncorrectAnnotationException extends RuntimeException {

    public IncorrectAnnotationException(Class annotatedClass) {
        super(
                String.format(
                        "Empty value member for @%s annotation in %s class.",
                        GraphQLTypeResolver.class.getSimpleName(),
                        annotatedClass.getName()
                )
        );
    }

}
