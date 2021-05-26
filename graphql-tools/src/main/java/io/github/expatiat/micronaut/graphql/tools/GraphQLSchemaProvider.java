package io.github.expatiat.micronaut.graphql.tools;

import graphql.schema.GraphQLSchema;

import javax.inject.Provider;

/**
 * @author Alexey Zhokhov
 */
// TODO late init provider
class GraphQLSchemaProvider implements Provider<GraphQLSchema> {

    private GraphQLSchema graphQLSchema;

    @Override
    public GraphQLSchema get() {
        if (graphQLSchema == null) {
            // TODO custom exception
            throw new RuntimeException("Provider is not initialized");
        }
        return graphQLSchema;
    }

    public void init(GraphQLSchema graphQLSchema) {
        if (this.graphQLSchema != null) {
            // TODO custom exception
            throw new RuntimeException("Provider already initialized");
        }
        this.graphQLSchema = graphQLSchema;
    }

}
