package io.micronaut.graphql.tools.exceptions;

import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ObjectTypeDefinition;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import static io.micronaut.core.util.ArgumentUtils.requireNonNull;
import static io.micronaut.core.util.ArgumentUtils.requirePositive;

public class MappingDetails {

    private final ObjectTypeDefinition objectTypeDefinition;
    private final FieldDefinition fieldDefinition;
    private final Integer argumentIndex;
    private final Class<?> mappedClass;
    private final String mappedMethod;

    public static MappingDetails forField(@NonNull ObjectTypeDefinition objectTypeDefinition,
                                          @NonNull FieldDefinition fieldDefinition) {
        requireNonNull("objectTypeDefinition", objectTypeDefinition);
        requireNonNull("fieldDefinition", fieldDefinition);

        return new MappingDetails(objectTypeDefinition, fieldDefinition, null, null, null);
    }

    public static MappingDetails forField(@NonNull ObjectTypeDefinition objectTypeDefinition,
                                          @NonNull FieldDefinition fieldDefinition,
                                          @NonNull Class<?> mappedClass, @Nullable String mappedMethod) {
        requireNonNull("objectTypeDefinition", objectTypeDefinition);
        requireNonNull("fieldDefinition", fieldDefinition);
        requireNonNull("mappedClass", mappedClass);

        return new MappingDetails(objectTypeDefinition, fieldDefinition, null, mappedClass, mappedMethod);
    }

    public static MappingDetails forField(@NonNull MappingDetails mappingDetails,
                                          @NonNull Class<?> mappedClass, @Nullable String mappedMethod) {
        requireNonNull("mappingDetails", mappingDetails);
        requireNonNull("mappedClass", mappedClass);

        return new MappingDetails(mappingDetails.getObjectTypeDefinition(), mappingDetails.getFieldDefinition(),
                null, mappedClass, mappedMethod);
    }

    public static MappingDetails forArgument(@NonNull MappingDetails mappingDetails,
                                             int position) {
        requireNonNull("mappingDetails", mappingDetails);
        requirePositive("position", position);

        return new MappingDetails(
                mappingDetails.getObjectTypeDefinition(),
                mappingDetails.getFieldDefinition(),
                position,
                mappingDetails.getMappedClass(),
                mappingDetails.getMappedMethod()
        );
    }

    private MappingDetails(ObjectTypeDefinition objectTypeDefinition, FieldDefinition fieldDefinition, @Nullable Integer argumentIndex,
                           @Nullable Class<?> mappedClass, @Nullable String mappedMethod) {
        this.objectTypeDefinition = objectTypeDefinition;
        this.fieldDefinition = fieldDefinition;
        this.argumentIndex = argumentIndex;
        this.mappedClass = mappedClass;
        this.mappedMethod = mappedMethod;
    }

    public ObjectTypeDefinition getObjectTypeDefinition() {
        return objectTypeDefinition;
    }

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    @Nullable
    public InputValueDefinition getInputValueDefinition() {
        if (argumentIndex != null) {
            return fieldDefinition.getInputValueDefinitions().get(0);
        }
        return null;
    }

    public String getGraphQlType() {
        return objectTypeDefinition.getName();
    }

    public String getGraphQlField() {
        return fieldDefinition.getName();
    }

    @Nullable
    public String getGraphQlArgument() {
        InputValueDefinition inputValueDefinition = getInputValueDefinition();
        if (inputValueDefinition != null) {
            return inputValueDefinition.getName();
        }
        return null;
    }

    @Nullable
    public Integer getGraphQlArgumentIndex() {
        return argumentIndex;
    }

    @Nullable
    public Class<?> getMappedClass() {
        return mappedClass;
    }

    @Nullable
    public String getMappedMethod() {
        return mappedMethod;
    }

    public String getMessage(String superMessage) {
        StringBuilder builder = new StringBuilder(superMessage);

        builder.append("\n  GraphQL type: ").append(getGraphQlType());
        builder.append("\n  GraphQL field: ").append(getGraphQlField());

        if (getGraphQlArgumentIndex() != null) {
            builder.append("\n  GraphQL argument: ").append(getGraphQlArgument());
        }

        if (mappedClass != null) {
            builder.append("\n  Mapped class: ").append(mappedClass.getName());
        }
        if (mappedMethod != null) {
            builder.append("\n  Mapped method: ").append(mappedMethod);
        }

        return builder.toString();
    }

}
