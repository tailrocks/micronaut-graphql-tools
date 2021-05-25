package com.example.graphql.query;

import com.example.graphql.model.PayloadError;
import com.example.graphql.model.SecurityError;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLRootResolver;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Alexey Zhokhov
 */
@GraphQLRootResolver
public class UnionTestQuery {

    public CompletionStage<PayloadError> unionTest() {
        return CompletableFuture.completedFuture(
                new SecurityError("abc-xyz")
        );
    }

}
