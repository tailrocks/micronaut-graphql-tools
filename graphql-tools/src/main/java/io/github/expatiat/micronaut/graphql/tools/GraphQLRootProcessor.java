package io.github.expatiat.micronaut.graphql.tools;

import com.example.graphql.impl.annotation.GraphQLRootResolver;
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

    private final com.example.graphql.impl.GraphQLMappingContext graphQLMappingContext;

    public GraphQLRootProcessor(com.example.graphql.impl.GraphQLMappingContext graphQLMappingContext) {
        this.graphQLMappingContext = graphQLMappingContext;
    }

    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        graphQLMappingContext.registerRootExecutableMethod(beanDefinition, method);
    }

}
