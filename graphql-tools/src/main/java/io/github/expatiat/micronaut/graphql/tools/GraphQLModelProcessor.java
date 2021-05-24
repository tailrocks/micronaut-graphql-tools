package io.github.expatiat.micronaut.graphql.tools;

import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLModelResolver;
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
public class GraphQLModelProcessor implements ExecutableMethodProcessor<GraphQLModelResolver> {

    private final GraphQLMappingContext graphQLMappingContext;

    public GraphQLModelProcessor(GraphQLMappingContext graphQLMappingContext) {
        this.graphQLMappingContext = graphQLMappingContext;
    }

    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        graphQLMappingContext.registerModelExecutableMethod(beanDefinition, method);
    }

}
