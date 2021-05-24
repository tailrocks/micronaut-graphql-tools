package io.github.expatiat.micronaut.graphql.tools;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

/**
 * @author Alexey Zhokhov
 */
@Factory
public class GraphQLFactory {

    @Bean
    @Singleton
    public GraphQL graphQL(GraphQLMappingContext graphQLMappingContext, TypeDefinitionRegistry typeRegistry) {
        SchemaGenerator schemaGenerator = new SchemaGenerator();

        RuntimeWiring runtimeWiring = graphQLMappingContext.generateRuntimeWiring(typeRegistry);

        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);

        return GraphQL.newGraphQL(graphQLSchema).build();
    }

}
