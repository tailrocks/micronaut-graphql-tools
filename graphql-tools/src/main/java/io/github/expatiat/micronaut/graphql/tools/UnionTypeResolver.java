package io.github.expatiat.micronaut.graphql.tools;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;

import javax.inject.Provider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexey Zhokhov
 */
public class UnionTypeResolver implements TypeResolver {

    private final Provider<GraphQLSchema> graphQLSchemaProvider;

    // source class -> target GraphQL type name
    private final Map<Class, String> objectTypes;

    public UnionTypeResolver(Provider<GraphQLSchema> graphQLSchemaProvider,
                             Map<Class, String> objectTypes) {
        this.graphQLSchemaProvider = graphQLSchemaProvider;
        this.objectTypes = new ConcurrentHashMap<>(objectTypes);
    }

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment env) {
        String graphQlType = objectTypes.get(env.getObject().getClass());

        if (graphQlType == null) {
            // TODO custom exception
            throw new RuntimeException("Unregistered GraphQL type: " + env.getObject().getClass());
        }

        GraphQLObjectType graphQLObjectType = graphQLSchemaProvider.get().getObjectType(graphQlType);
        return graphQLObjectType;
    }

}
