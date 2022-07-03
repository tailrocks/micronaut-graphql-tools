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
package io.micronaut.graphql.tools.processor;

import io.micronaut.context.annotation.Infrastructure;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.Internal;
import io.micronaut.graphql.tools.GraphQLResolversRegistry;
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import jakarta.inject.Singleton;

/**
 * @author Alexey Zhokhov
 */
@Internal
@Singleton
@Infrastructure
public class GraphQLRootResolverMethodProcessor implements ExecutableMethodProcessor<GraphQLRootResolver> {

    private final GraphQLResolversRegistry graphQLResolversRegistry;

    public GraphQLRootResolverMethodProcessor(GraphQLResolversRegistry graphQLResolversRegistry) {
        this.graphQLResolversRegistry = graphQLResolversRegistry;
    }

    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        graphQLResolversRegistry.registerRootResolverExecutableMethod(beanDefinition, (ExecutableMethod<Object, ?>) method);
    }

}
