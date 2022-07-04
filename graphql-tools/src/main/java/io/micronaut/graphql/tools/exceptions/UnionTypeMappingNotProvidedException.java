package io.micronaut.graphql.tools.exceptions;

import io.micronaut.graphql.tools.MappingContext;
import io.micronaut.graphql.tools.SchemaParserDictionary;

public class UnionTypeMappingNotProvidedException extends AbstractMappingException {

    public UnionTypeMappingNotProvidedException(MappingContext mappingContext, String objectType, String unionType) {
        super(
                String.format(
                        "Can not detect representation class for type %s, member of %s union. " +
                                "Ensure the representation class is registered via %s.",
                        objectType,
                        unionType,
                        SchemaParserDictionary.class.getName()
                ),
                mappingContext
        );
    }

}
