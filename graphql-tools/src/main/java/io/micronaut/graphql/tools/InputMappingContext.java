package io.micronaut.graphql.tools;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.util.Optional;

import static io.micronaut.core.util.ArgumentUtils.requireNonNull;

public class InputMappingContext implements MappingContext {

    private final InputObjectTypeDefinition inputObjectTypeDefinition;
    private final String inputValue;

    private final Class<?> mappedClass;

    public InputMappingContext(@NonNull InputObjectTypeDefinition inputObjectTypeDefinition,
                               @NonNull String inputValue,
                               @NonNull Class<?> mappedClass) {
        requireNonNull("inputObjectTypeDefinition", inputObjectTypeDefinition);
        requireNonNull("inputValue", inputValue);
        requireNonNull("mappedClass", mappedClass);

        this.inputObjectTypeDefinition = inputObjectTypeDefinition;
        this.inputValue = inputValue;
        this.mappedClass = mappedClass;
    }

    public InputMappingContext(@NonNull InputObjectTypeDefinition inputObjectTypeDefinition,
                               @Nullable Class<?> mappedClass) {
        requireNonNull("inputObjectTypeDefinition", inputObjectTypeDefinition);

        this.inputObjectTypeDefinition = inputObjectTypeDefinition;
        this.inputValue = null;
        this.mappedClass = mappedClass;
    }

    @Override
    @Nullable
    public Class<?> getMappedClass() {
        return mappedClass;
    }

    @Override
    public String getMessage(String superMessage) {
        StringBuilder builder = new StringBuilder(superMessage);

        builder.append("\n  GraphQL input: ").append(getGraphQlInput());

        if (inputValue != null) {
            builder.append("\n  GraphQL input value: ").append(inputValue);
        }

        if (mappedClass != null) {
            builder.append("\n  Mapped class: ").append(mappedClass.getName());
        }

        return builder.toString();
    }

    public InputObjectTypeDefinition getInputObjectTypeDefinition() {
        return inputObjectTypeDefinition;
    }

    public String getGraphQlInput() {
        return inputObjectTypeDefinition.getName();
    }

    @Nullable
    public String getGraphQLInputValue() {
        return inputValue;
    }

    public Optional<InputValueDefinition> getInputValueDefinition() {
        if (inputValue == null) {
            return Optional.empty();
        }
        return inputObjectTypeDefinition.getInputValueDefinitions().stream()
                .filter(it -> it.getName().equals(inputValue))
                .findFirst();
    }

}
