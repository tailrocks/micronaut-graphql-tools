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
