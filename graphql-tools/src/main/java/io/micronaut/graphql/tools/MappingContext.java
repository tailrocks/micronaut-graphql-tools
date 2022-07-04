/*
 * Copyright 2021-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.graphql.tools;

import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ObjectTypeDefinition;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import static io.micronaut.core.util.ArgumentUtils.requireNonNull;
import static io.micronaut.core.util.ArgumentUtils.requirePositive;

/**
 * @author Alexey Zhokhov
 */
public class MappingContext {

    private final ObjectTypeDefinition objectTypeDefinition;
    private final FieldDefinition fieldDefinition;
    private final Integer argumentIndex;
    private final Class<?> mappedClass;
    private final String mappedMethod;

    public static MappingContext forField(@NonNull ObjectTypeDefinition objectTypeDefinition,
                                          @NonNull FieldDefinition fieldDefinition) {
        requireNonNull("objectTypeDefinition", objectTypeDefinition);
        requireNonNull("fieldDefinition", fieldDefinition);

        return new MappingContext(objectTypeDefinition, fieldDefinition, null, null, null);
    }

    public static MappingContext forField(@NonNull ObjectTypeDefinition objectTypeDefinition,
                                          @NonNull FieldDefinition fieldDefinition,
                                          @NonNull Class<?> mappedClass, @Nullable String mappedMethod) {
        requireNonNull("objectTypeDefinition", objectTypeDefinition);
        requireNonNull("fieldDefinition", fieldDefinition);
        requireNonNull("mappedClass", mappedClass);

        return new MappingContext(objectTypeDefinition, fieldDefinition, null, mappedClass, mappedMethod);
    }

    public static MappingContext forField(@NonNull MappingContext mappingContext,
                                          @NonNull Class<?> mappedClass, @Nullable String mappedMethod) {
        requireNonNull("mappingDetails", mappingContext);
        requireNonNull("mappedClass", mappedClass);

        return new MappingContext(mappingContext.getObjectTypeDefinition(), mappingContext.getFieldDefinition(),
                null, mappedClass, mappedMethod);
    }

    public static MappingContext forArgument(@NonNull MappingContext mappingContext,
                                             int position) {
        requireNonNull("mappingDetails", mappingContext);
        requirePositive("position", position);

        return new MappingContext(
                mappingContext.getObjectTypeDefinition(),
                mappingContext.getFieldDefinition(),
                position,
                mappingContext.getMappedClass(),
                mappingContext.getMappedMethod()
        );
    }

    private MappingContext(ObjectTypeDefinition objectTypeDefinition, FieldDefinition fieldDefinition, @Nullable Integer argumentIndex,
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
            return fieldDefinition.getInputValueDefinitions().get(argumentIndex);
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
