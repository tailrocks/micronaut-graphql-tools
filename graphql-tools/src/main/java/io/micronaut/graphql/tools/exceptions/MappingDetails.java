package io.micronaut.graphql.tools.exceptions;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import static io.micronaut.core.util.ArgumentUtils.requireNonNull;

public class MappingDetails {

    private final ObjectTypeDefinition objectTypeDefinition;
    private final FieldDefinition fieldDefinition;
    private final Class<?> mappedClass;
    private final String mappedMethod;

    public static MappingDetails forField(@NonNull ObjectTypeDefinition objectTypeDefinition,
                                          @NonNull FieldDefinition fieldDefinition) {
        requireNonNull("objectTypeDefinition", objectTypeDefinition);
        requireNonNull("fieldDefinition", fieldDefinition);

        return new MappingDetails(objectTypeDefinition, fieldDefinition, null, null);
    }

    public static MappingDetails forField(@NonNull ObjectTypeDefinition objectTypeDefinition,
                                          @NonNull FieldDefinition fieldDefinition,
                                          @NonNull Class<?> mappedClass, @Nullable String mappedMethod) {
        requireNonNull("objectTypeDefinition", objectTypeDefinition);
        requireNonNull("fieldDefinition", fieldDefinition);
        requireNonNull("mappedClass", mappedClass);

        return new MappingDetails(objectTypeDefinition, fieldDefinition, mappedClass, mappedMethod);
    }

    public static MappingDetails forField(@NonNull MappingDetails mappingDetails,
                                          @NonNull Class<?> mappedClass, @Nullable String mappedMethod) {
        requireNonNull("mappingDetails", mappingDetails);
        requireNonNull("mappedClass", mappedClass);

        return new MappingDetails(mappingDetails.getObjectTypeDefinition(), mappingDetails.getFieldDefinition(),
                mappedClass, mappedMethod);
    }

    private MappingDetails(ObjectTypeDefinition objectTypeDefinition, FieldDefinition fieldDefinition,
                           @Nullable Class<?> mappedClass, @Nullable String mappedMethod) {
        this.objectTypeDefinition = objectTypeDefinition;
        this.fieldDefinition = fieldDefinition;
        this.mappedClass = mappedClass;
        this.mappedMethod = mappedMethod;
    }

    public ObjectTypeDefinition getObjectTypeDefinition() {
        return objectTypeDefinition;
    }

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public String getGraphQlType() {
        return objectTypeDefinition.getName();
    }

    public String getGraphQlField() {
        return fieldDefinition.getName();
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

        if (mappedClass != null) {
            builder.append("\n  Mapped class: ").append(mappedClass.getName());
        }
        if (mappedMethod != null) {
            builder.append("\n  Mapped method: ").append(mappedMethod);
        }

        return builder.toString();
    }

}
