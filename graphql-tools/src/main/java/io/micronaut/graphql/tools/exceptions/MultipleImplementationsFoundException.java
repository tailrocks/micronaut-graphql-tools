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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alexey Zhokhov
 */
public class MultipleImplementationsFoundException extends AbstractMappingException {

    private final List<Class<?>> implementationClasses;

    public MultipleImplementationsFoundException(MappingContext mappingContext, Class<?> interfaceClass,
                                                 List<Class<?>> implementationClasses) {
        super(
                String.format(
                        "Found multiple implementations for the interface %s.",
                        interfaceClass.getName()
                ),
                mappingContext
        );

        this.implementationClasses = implementationClasses.stream()
                .sorted(Comparator.comparing(Class::getName))
                .collect(Collectors.toList());
    }

    public List<Class<?>> getImplementationClasses() {
        return implementationClasses;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "\n  Implementation classes: " +
                implementationClasses.stream()
                        .map(Class::getName)
                        .collect(Collectors.joining(", "));
    }

}
