package example;

import io.micronaut.graphql.tools.annotation.GraphQLRootResolver;

@GraphQLRootResolver
public class AddBookMutation {

    public AddBookPayload addBook(String id) {
        return null;
    }

}
