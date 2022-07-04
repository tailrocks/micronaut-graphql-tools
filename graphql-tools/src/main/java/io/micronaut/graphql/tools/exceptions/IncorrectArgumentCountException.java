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

import io.micronaut.core.annotation.Nullable;
import io.micronaut.graphql.tools.MappingContext;

/**
 * @author Alexey Zhokhov
 */
public class IncorrectArgumentCountException extends AbstractMappingException {

    private final int providedCount;
    private final int requiredCount;

    public IncorrectArgumentCountException(MappingContext mappingContext, boolean few, int providedCount,
                                           int requiredCount, @Nullable String requiredMethodArgs) {
        super(
                String.format(
                        "The method has too %s arguments, provided: %s, required %s arg(s)%s",
                        few ? "few" : "many",
                        providedCount,
                        requiredCount,
                        requiredMethodArgs != null ? ": " + requiredMethodArgs : "."
                ),
                mappingContext
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
