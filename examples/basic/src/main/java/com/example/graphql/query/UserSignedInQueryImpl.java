package com.example.graphql.query;

import com.example.graphql.api.User;
import com.example.graphql.api.UserSignedInQuery;
import com.example.graphql.model.UserImpl;
import graphql.schema.DataFetchingEnvironment;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLRootResolver;
import io.micronaut.core.annotation.Nullable;

/**
 * @author Alexey Zhokhov
 */
@GraphQLRootResolver
public class UserSignedInQueryImpl implements UserSignedInQuery {

    @Override
    @Nullable
    public User userSignedIn(DataFetchingEnvironment env) {
        return new UserImpl("me@test.com");
    }

}
