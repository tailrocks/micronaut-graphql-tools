package io.micronaut.graphql.tools.exceptions;

import java.util.List;
import java.util.stream.Collectors;

public class MissingEnumValuesException extends AbstractMappingMethodException {

    private final List<String> missingValues;

    public MissingEnumValuesException(String graphQlTypeName, String graphQlFieldName, Class mappedClass,
                                      String mappedMethod, List<String> missingValues) {
        super(
                "Some enum values are missing.",
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethod
        );

        this.missingValues = missingValues;
    }

    public List<String> getMissingValues() {
        return missingValues;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());

        builder.append("\n  Missing values: ").append(
                missingValues.stream()
                        .sorted()
                        .collect(Collectors.joining(", "))
        );

        return builder.toString();
    }

}
