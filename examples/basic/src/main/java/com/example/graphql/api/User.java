package com.example.graphql.api;

import io.micronaut.core.annotation.NonNull;

/**
 * @author Alexey Zhokhov
 */
public interface User {

    @NonNull
    String getEmail();

}
