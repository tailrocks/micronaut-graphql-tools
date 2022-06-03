package io.micronaut.graphql.tools.exceptions;

// TODO deprecated
public abstract class AbstractMappingMethodException extends AbstractMappingException {

    public AbstractMappingMethodException(String message, MappingDetails mappingDetails) {
        super(message, mappingDetails);
    }

}
