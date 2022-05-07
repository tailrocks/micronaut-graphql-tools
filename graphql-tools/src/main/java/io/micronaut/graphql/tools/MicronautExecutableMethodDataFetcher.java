/*
 * Copyright 2017-2022 original authors
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

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Executable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Alexey Zhokhov
 */
public class MicronautExecutableMethodDataFetcher implements DataFetcher<Object> {

    private final ObjectMapper objectMapper;
    private final Executable<Object, Object> executable;
    private final List<ArgumentDetails> argumentDetailsList;

    @Nullable
    private final Object instance;

    public MicronautExecutableMethodDataFetcher(
            ObjectMapper objectMapper,
            Executable<Object, Object> executable,
            List<ArgumentDetails> argumentDetailsList,
            @Nullable Object instance
    ) {
        this.objectMapper = objectMapper;
        this.executable = executable;
        this.argumentDetailsList = new CopyOnWriteArrayList<>(argumentDetailsList);
        this.instance = instance;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        List<Object> arguments = new ArrayList<>();

        for (ArgumentDetails argumentDetails : argumentDetailsList) {
            if (argumentDetails.getInputClass().isPresent()) {
                Object argumentValue = environment.getArgument(argumentDetails.getName());

                if (argumentValue != null) {
                    Object convertedValue = objectMapper.convertValue(argumentValue, argumentDetails.getInputClass().get());

                    arguments.add(convertedValue);
                } else {
                    arguments.add(null);
                }
            } else if (argumentDetails.getName().equals(ArgumentDetails.SOURCE_ARGUMENT)) {
                arguments.add(environment.getSource());
            } else if (argumentDetails.getName().equals(ArgumentDetails.DATA_FETCHING_ENVIRONMENT_ARGUMENT)) {
                arguments.add(environment);
            } else {
                arguments.add(environment.getArgument(argumentDetails.getName()));
            }
        }

        if (instance != null) {
            return executable.invoke(instance, arguments.toArray());
        } else {
            return executable.invoke(environment.getSource(), arguments.toArray());
        }
    }

}
