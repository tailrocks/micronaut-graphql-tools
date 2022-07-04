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

import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import io.micronaut.core.annotation.Internal;

@Internal
final class GraphQLUtils {

    private GraphQLUtils() {
    }

    static TypeName getTypeName(Type<?> type) {
        type = unwrapNonNullType(type);
        if (type instanceof TypeName) {
            return ((TypeName) type);
        }
        throw new UnsupportedOperationException("Unknown type: " + type);
    }

    static Type<?> unwrapNonNullType(Type<?> type) {
        if (type instanceof NonNullType) {
            return unwrapNonNullType(((NonNullType) type).getType());
        }
        return type;
    }

}
