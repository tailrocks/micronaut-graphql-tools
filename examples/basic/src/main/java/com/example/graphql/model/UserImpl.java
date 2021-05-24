package com.example.graphql.model;

import com.example.graphql.api.User;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLModel;
import io.micronaut.core.annotation.NonNull;

/**
 * @author Alexey Zhokhov
 */
@GraphQLModel(User.class)
public class UserImpl implements User {

    private final String email;

    public UserImpl(String email) {
        this.email = email;
    }

    @Override
    @NonNull
    public String getEmail() {
        return email;
    }

}
