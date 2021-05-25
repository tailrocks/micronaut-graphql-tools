package io.github.expatiat.micronaut.graphql.tools;

import io.micronaut.context.annotation.Requires;

import javax.inject.Singleton;

/**
 * @author Alexey Zhokhov
 */
@Singleton
@Requires(missingBeans = {SchemaParserDictionaryCustomizer.class})
public class DefaultSchemaParserDictionaryCustomizer implements SchemaParserDictionaryCustomizer {

    @Override
    public void customize(SchemaParserDictionary schemaParserDictionary) {
        // do nothing, can be overridden with another implementation
    }

}
