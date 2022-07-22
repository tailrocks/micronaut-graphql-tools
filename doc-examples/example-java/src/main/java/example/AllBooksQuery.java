package example;

import io.micronaut.graphql.tools.annotation.GraphQLRootResolver;

import java.util.List;

@GraphQLRootResolver
public class AllBooksQuery {

    public List<Book> allBooks() {
        return null;
    }

}
