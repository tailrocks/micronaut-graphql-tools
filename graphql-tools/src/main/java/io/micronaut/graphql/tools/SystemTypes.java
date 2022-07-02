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

import graphql.language.TypeName;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// TODO unmodified types
public final class SystemTypes {

    private static final Map<String, Set<Class<?>>> SYSTEM_TYPES = new HashMap<>();

    // https://www.graphql-java.com/documentation/scalars
    static {
        SYSTEM_TYPES.put("String", new HashSet<>(Collections.singletonList(String.class)));
        SYSTEM_TYPES.put("Boolean", new HashSet<>(Arrays.asList(boolean.class, Boolean.class)));
        SYSTEM_TYPES.put("Int", new HashSet<>(Arrays.asList(int.class, Integer.class)));
        SYSTEM_TYPES.put("Float", new HashSet<>(Arrays.asList(float.class, Float.class)));
        SYSTEM_TYPES.put("ID", new HashSet<>(Collections.singletonList(String.class)));

        SYSTEM_TYPES.put("Long", new HashSet<>(Arrays.asList(long.class, Long.class)));
        SYSTEM_TYPES.put("Short", new HashSet<>(Arrays.asList(short.class, Short.class)));
        SYSTEM_TYPES.put("BigDecimal", new HashSet<>(Collections.singletonList(BigDecimal.class)));
        SYSTEM_TYPES.put("BigInteger", new HashSet<>(Collections.singletonList(BigInteger.class)));
    }

    private static final Set<Class<?>> SYSTEM_TYPES_CACHE = SYSTEM_TYPES.values().stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    public static Set<Class<?>> getSupportedClasses(@NonNull TypeName typeName) {
        ArgumentUtils.requireNonNull("typeName", typeName);
        return SYSTEM_TYPES.get(typeName.getName());
    }

    public static boolean isGraphQlBuiltInType(@NonNull TypeName typeName) {
        ArgumentUtils.requireNonNull("typeName", typeName);
        return SYSTEM_TYPES.containsKey(typeName.getName());
    }

    public static boolean isJavaBuiltInClass(@NonNull Class<?> clazz) {
        ArgumentUtils.requireNonNull("clazz", clazz);
        return SYSTEM_TYPES_CACHE.contains(clazz);
    }

}
