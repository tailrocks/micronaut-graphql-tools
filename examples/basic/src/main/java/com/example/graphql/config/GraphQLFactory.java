/*
 * Copyright 2021 original authors
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
package com.example.graphql.config;

import com.example.graphql.model.PayloadError;
import com.example.graphql.model.SecurityError;
import com.example.graphql.model.ValidationError;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.micronaut.graphql.tools.SchemaParserDictionaryCustomizer;
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

    @Bean
    @Singleton
    public SchemaParserDictionaryCustomizer schemaParserDictionaryCustomizer() {
        return schemaParserDictionary -> schemaParserDictionary
                .addType("SecurityError", SecurityError.class)
                .addType("ValidationError", ValidationError.class)
                .addUnion("PayloadError", PayloadError.class);
    }

}
