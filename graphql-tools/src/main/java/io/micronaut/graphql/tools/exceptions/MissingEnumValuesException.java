package io.micronaut.graphql.tools.exceptions;

import java.util.List;
import java.util.stream.Collectors;

public class MissingEnumValuesException extends AbstractMappingMethodException {

    private final List<String> missingValues;

    public MissingEnumValuesException(MappingDetails mappingDetails, List<String> missingValues) {
        super(
                "Some enum values are missing.",
                mappingDetails
        );

        this.missingValues = missingValues;
    }

    public List<String> getMissingValues() {
        return missingValues;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "\n  Missing values: " +
                missingValues.stream()
                        .sorted()
                        .collect(Collectors.joining(", "));
    }

}
