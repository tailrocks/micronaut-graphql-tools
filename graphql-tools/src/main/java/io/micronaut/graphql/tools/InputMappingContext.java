package io.micronaut.graphql.tools;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.util.Optional;

import static io.micronaut.core.util.ArgumentUtils.requireNonNull;

public class InputMappingContext implements MappingContext {

    private final InputObjectTypeDefinition inputObjectTypeDefinition;
    private final String inputValueName;

    private final Class<?> mappedClass;
    private final String mappedProperty;

    public InputMappingContext(@NonNull InputObjectTypeDefinition inputObjectTypeDefinition,
                               @NonNull String inputValueName,
                               @NonNull Class<?> mappedClass,
                               @Nullable String mappedProperty) {
        requireNonNull("inputObjectTypeDefinition", inputObjectTypeDefinition);
        requireNonNull("inputValueName", inputValueName);
        requireNonNull("mappedClass", mappedClass);

        this.inputObjectTypeDefinition = inputObjectTypeDefinition;
        this.inputValueName = inputValueName;
        this.mappedClass = mappedClass;
        this.mappedProperty = mappedProperty;
    }

    public InputMappingContext(@NonNull InputObjectTypeDefinition inputObjectTypeDefinition,
                               @Nullable Class<?> mappedClass) {
        requireNonNull("inputObjectTypeDefinition", inputObjectTypeDefinition);

        this.inputObjectTypeDefinition = inputObjectTypeDefinition;
        this.inputValueName = null;
        this.mappedClass = mappedClass;
        this.mappedProperty = null;
    }

    @Override
    @Nullable
    public Class<?> getMappedClass() {
        return mappedClass;
    }

    @Nullable
    public String getMappedProperty() {
        return mappedProperty;
    }

    @Override
    public String getMessage(String superMessage) {
        StringBuilder builder = new StringBuilder(superMessage);

        builder.append("\n  GraphQL input object type: ").append(getGraphQlInputObjectType());

        if (inputValueName != null) {
            builder.append("\n  GraphQL input value: ").append(inputValueName);
        }

        if (mappedClass != null) {
            builder.append("\n  Mapped class: ").append(mappedClass.getName());
        }

        if (mappedProperty != null) {
            builder.append("\n  Mapped property: ").append(mappedProperty);
        }

        return builder.toString();
    }

    public InputObjectTypeDefinition getInputObjectTypeDefinition() {
        return inputObjectTypeDefinition;
    }

    public String getGraphQlInputObjectType() {
        return inputObjectTypeDefinition.getName();
    }

    @Nullable
    public String getGraphQlInputValue() {
        return inputValueName;
    }

    public Optional<InputValueDefinition> getInputValueDefinition() {
        if (inputValueName == null) {
            return Optional.empty();
        }
        return inputObjectTypeDefinition.getInputValueDefinitions().stream()
                .filter(it -> it.getName().equals(inputValueName))
                .findFirst();
    }

}
