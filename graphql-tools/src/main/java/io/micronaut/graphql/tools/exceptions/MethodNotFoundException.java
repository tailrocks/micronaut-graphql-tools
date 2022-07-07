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

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alexey Zhokhov
 */
public class MethodNotFoundException extends RuntimeException {

    private final String methodName;

    public static MethodNotFoundException forRoot(String methodName, List<Class<?>> resolvers) {
        return new MethodNotFoundException(
                String.format(
                        "The method `%s` not found in any root resolvers: %s.",
                        methodName,
                        "[" + resolvers.stream()
                                .map(Class::getName)
                                .collect(Collectors.joining(", ")) + "]"
                ),
                methodName
        );
    }

    public static MethodNotFoundException forType(String methodName, Class<?> objectTypeClass,
                                                  List<Class<?>> resolvers) {
        return new MethodNotFoundException(
                String.format(
                       resolvers.isEmpty()
                               ? "The property or method `%s` not found in %s's type."
                               : "The property or method `%s` not found in %s's type or it resolvers: %s.",
                        methodName,
                        objectTypeClass.getName(),
                        "[" + resolvers.stream()
                                .map(Class::getName)
                                .collect(Collectors.joining(", ")) + "]"
                ),
                methodName
        );
    }

    private MethodNotFoundException(String message, String methodName) {
        super(message);
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

}
