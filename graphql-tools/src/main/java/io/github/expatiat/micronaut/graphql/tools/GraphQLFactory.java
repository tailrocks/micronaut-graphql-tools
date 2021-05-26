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
    public GraphQL graphQL(GraphQLMappingContext graphQLMappingContext, TypeDefinitionRegistry typeRegistry,
                           SchemaParserDictionaryCustomizer schemaParserDictionaryCustomizer) {
        SchemaParserDictionary schemaParserDictionary = new SchemaParserDictionary();
        schemaParserDictionaryCustomizer.customize(schemaParserDictionary);

        GraphQLSchemaProvider graphQLSchemaProvider = new GraphQLSchemaProvider();

        RuntimeWiring runtimeWiring = graphQLMappingContext.generateRuntimeWiring(typeRegistry, schemaParserDictionary,
                graphQLSchemaProvider);

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);

        graphQLSchemaProvider.init(graphQLSchema);

        return GraphQL.newGraphQL(graphQLSchema).build();
    }

}
