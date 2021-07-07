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
package io.micronaut.graphql.tools;

import io.micronaut.graphql.tools.annotation.GraphQLRootResolver;
import io.micronaut.context.annotation.Infrastructure;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;

import javax.inject.Singleton;

/**
 * @author Alexey Zhokhov
 */
@Singleton
@Infrastructure
public class GraphQLRootProcessor implements ExecutableMethodProcessor<GraphQLRootResolver> {

    private final GraphQLMappingContext graphQLMappingContext;

    public GraphQLRootProcessor(GraphQLMappingContext graphQLMappingContext) {
        this.graphQLMappingContext = graphQLMappingContext;
    }

    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        graphQLMappingContext.registerRootExecutableMethod(beanDefinition, method);
    }

}
