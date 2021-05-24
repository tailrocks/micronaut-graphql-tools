package com.example.graphql.api;

import graphql.schema.DataFetchingEnvironment;
import io.micronaut.core.annotation.Nullable;

/**
 * @author Alexey Zhokhov
 */
public interface UserSignedInQuery {

    @Nullable
    User userSignedIn(DataFetchingEnvironment env);

}
