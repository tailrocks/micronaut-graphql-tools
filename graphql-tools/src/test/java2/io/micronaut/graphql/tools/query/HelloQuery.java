package io.micronaut.graphql.tools.query;

import io.micronaut.graphql.tools.annotation.GraphQLRootResolver;

@GraphQLRootResolver
public class HelloQuery {

    public String hello() {
        return "World!";
    }

}
