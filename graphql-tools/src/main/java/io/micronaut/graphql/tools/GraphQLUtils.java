package io.micronaut.graphql.tools;

import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;

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
