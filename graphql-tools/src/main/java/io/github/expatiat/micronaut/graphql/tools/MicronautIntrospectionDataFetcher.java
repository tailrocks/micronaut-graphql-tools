package io.github.expatiat.micronaut.graphql.tools;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;

/**
 * @author Alexey Zhokhov
 */
public class MicronautIntrospectionDataFetcher implements DataFetcher<Object> {

    private final BeanIntrospection<Object> beanIntrospection;

    public MicronautIntrospectionDataFetcher(BeanIntrospection beanIntrospection) {
        this.beanIntrospection = beanIntrospection;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        String fieldName = environment.getField().getName();

        BeanProperty<?, Object> property = beanIntrospection.getProperty(fieldName)
                // TODO custom exception
                .orElseThrow(() -> new RuntimeException("Property `" + fieldName + "` not found: " + beanIntrospection.getBeanType()));

        return property.get(environment.getSource());
    }

}
