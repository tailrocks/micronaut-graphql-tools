package io.github.expatiat.micronaut.graphql.tools;

import io.micronaut.core.util.ArgumentUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Alexey Zhokhov
 */
public class SchemaParserDictionary {

    private final Map<String, Class> types = new LinkedHashMap<>();
    private final Map<String, Class> unions = new LinkedHashMap<>();

    public Map<String, Class> getTypes() {
        return Collections.unmodifiableMap(types);
    }

    public Map<String, Class> getUnions() {
        return Collections.unmodifiableMap(unions);
    }

    public SchemaParserDictionary addType(String graphqlType, Class implementationClass) {
        ArgumentUtils.requireNonNull("graphqlType", graphqlType);
        ArgumentUtils.requireNonNull("implementationClass", implementationClass);

        if (
                implementationClass.isInterface()
                        || implementationClass.isEnum()
                        || implementationClass.isArray()
                        || implementationClass.isAnnotation()
        ) {
            throw new IllegalArgumentException(implementationClass + " must be a top level class.");
        }

        if (types.containsKey(graphqlType)) {
            throw new IllegalArgumentException("Duplicated GraphQL type: " + graphqlType);
        }
        if (types.containsValue(implementationClass)) {
            throw new IllegalArgumentException("One GraphQL type can have only one implementation class, found duplicate: " + implementationClass);
        }
        types.put(graphqlType, implementationClass);

        return this;
    }

    public SchemaParserDictionary addUnion(String graphqlUnion, Class interfaceClass) {
        ArgumentUtils.requireNonNull("graphqlUnion", graphqlUnion);
        ArgumentUtils.requireNonNull("interfaceClass", interfaceClass);

        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException(interfaceClass + " must be an interface.");
        }

        if (unions.containsKey(graphqlUnion)) {
            throw new IllegalArgumentException("Duplicated GraphQL type: " + graphqlUnion);
        }
        if (unions.containsValue(interfaceClass)) {
            throw new IllegalArgumentException("One GraphQL union can have only one interface class, found duplicate: " + interfaceClass);
        }
        unions.put(graphqlUnion, interfaceClass);

        return this;
    }

}
