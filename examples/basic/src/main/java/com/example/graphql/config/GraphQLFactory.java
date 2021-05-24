package com.example.graphql.config;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.io.ResourceResolver;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Alexey Zhokhov
 */
@Factory
public class GraphQLFactory {

    @Bean
    @Singleton
    public TypeDefinitionRegistry typeDefinitionRegistry(ResourceResolver resourceResolver) {
        SchemaParser schemaParser = new SchemaParser();

        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        typeRegistry.merge(schemaParser.parse(new BufferedReader(new InputStreamReader(
                resourceResolver.getResourceAsStream("classpath:graphql/example.graphqls").get()))));

        return typeRegistry;
    }

}
