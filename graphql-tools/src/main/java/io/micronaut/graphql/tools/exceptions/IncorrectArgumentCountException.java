package io.micronaut.graphql.tools.exceptions;

import io.micronaut.core.annotation.Nullable;

public class IncorrectArgumentCountException extends AbstractMappingMethodException {

    private final int providedCount;
    private final int requiredCount;

    public IncorrectArgumentCountException(boolean few, String graphQlTypeName, String graphQlFieldName,
                                           Class mappedClass, String mappedMethod, int providedCount,
                                           int requiredCount, @Nullable String requiredMethodArgs) {
        super(
                String.format(
                        "The method has too %s arguments, provided: %s, required %s arg(s)%s",
                        few ? "few" : "many",
                        providedCount,
                        requiredCount,
                        requiredMethodArgs != null ? ": " + requiredMethodArgs : "."
                ),
                graphQlTypeName, graphQlFieldName, mappedClass, mappedMethod
        );

        this.providedCount = providedCount;
        this.requiredCount = requiredCount;
    }

    public int getProvidedCount() {
        return providedCount;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

}
