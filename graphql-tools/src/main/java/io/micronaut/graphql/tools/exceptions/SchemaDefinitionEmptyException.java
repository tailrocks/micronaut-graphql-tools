package io.micronaut.graphql.tools.exceptions;

public class SchemaDefinitionEmptyException extends RuntimeException {

    public SchemaDefinitionEmptyException() {
        super("Schema definition is not set. Make sure your GraphQL schema contains such definition:\n" +
                "  schema {\n" +
                "    query: Query\n" +
                "    mutation: Mutation\n" +
                "  }");
    }

}
