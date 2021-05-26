package com.example.graphql.query;

import com.example.graphql.model.PayloadError;
import com.example.graphql.model.SecurityError;
import com.example.graphql.model.ValidationError;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLRootResolver;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Alexey Zhokhov
 */
@GraphQLRootResolver
public class UnionTestQuery {

    public CompletionStage<PayloadError> unionTypeTest(Boolean securityError) {
        return CompletableFuture.completedFuture(
                securityError ? new SecurityError("abc-xyz") : new ValidationError(123)
        );
    }

}
