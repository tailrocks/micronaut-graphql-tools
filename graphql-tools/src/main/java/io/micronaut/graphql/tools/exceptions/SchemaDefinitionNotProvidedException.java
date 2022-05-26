package io.micronaut.graphql.tools.exceptions;

public class SchemaDefinitionNotProvidedException extends RuntimeException {

    public SchemaDefinitionNotProvidedException() {
        super("Schema definition is not set. Make sure your GraphQL schema contains such definition:\n" +
                "  schema {\n" +
                "    query: Query\n" +
                "    mutation: Mutation\n" +
                "  }");
    }

}
