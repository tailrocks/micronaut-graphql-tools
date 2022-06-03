package io.micronaut.graphql.tools.exceptions;

public abstract class AbstractMappingException extends RuntimeException {

    private final MappingDetails mappingDetails;

    public AbstractMappingException(
            String message,
            MappingDetails mappingDetails
    ) {
        super(message);
        this.mappingDetails = mappingDetails;
    }

    public MappingDetails getMappingDetails() {
        return mappingDetails;
    }

    @Override
    public String getMessage() {
        return mappingDetails.getMessage(super.getMessage());
    }

}
