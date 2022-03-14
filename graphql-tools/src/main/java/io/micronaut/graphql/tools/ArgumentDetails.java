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

import io.micronaut.core.annotation.Nullable;

import java.util.Optional;

/**
 * @author Alexey Zhokhov
 */
class ArgumentDetails {

    static final String SOURCE_ARGUMENT = "* SOURCE *";
    static final String DATA_FETCHING_ENVIRONMENT_ARGUMENT = "* DFE *";

    private final String name;

    @Nullable
    private final Class inputClass;

    public ArgumentDetails(String name, @Nullable Class inputClass) {
        this.name = name;
        this.inputClass = inputClass;
    }

    public String getName() {
        return name;
    }

    public Optional<Class> getInputClass() {
        return Optional.ofNullable(inputClass);
    }

}
