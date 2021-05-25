package io.github.expatiat.micronaut.graphql.tools;

import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLTypeResolver;
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
public class GraphQLTypeProcessor implements ExecutableMethodProcessor<GraphQLTypeResolver> {

    private final GraphQLMappingContext graphQLMappingContext;

    public GraphQLTypeProcessor(GraphQLMappingContext graphQLMappingContext) {
        this.graphQLMappingContext = graphQLMappingContext;
    }

    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        graphQLMappingContext.registerTypeExecutableMethod(beanDefinition, method);
    }

}
