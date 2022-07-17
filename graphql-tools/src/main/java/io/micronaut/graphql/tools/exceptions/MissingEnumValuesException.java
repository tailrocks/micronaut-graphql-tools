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
package io.micronaut.graphql.tools.exceptions;

import io.micronaut.graphql.tools.MappingContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alexey Zhokhov
 */
public final class MissingEnumValuesException extends AbstractMappingException {

    private final List<String> missingValues;

    public MissingEnumValuesException(MappingContext mappingContext, List<String> missingValues) {
        super(
                "Some enum values are missing.",
                mappingContext
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
