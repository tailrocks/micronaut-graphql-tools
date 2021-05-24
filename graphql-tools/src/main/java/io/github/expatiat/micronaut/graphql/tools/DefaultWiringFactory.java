package io.github.expatiat.micronaut.graphql.tools;

import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;

/**
 * @author Alexey Zhokhov
 */
public class DefaultWiringFactory implements WiringFactory {

    @Override
    public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
        throw new RuntimeException("Unprocessed type: " + environment.getParentType().getName());
    }

}
