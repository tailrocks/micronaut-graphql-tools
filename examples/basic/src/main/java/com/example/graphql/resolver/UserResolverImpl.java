package com.example.graphql.resolver;

import com.example.graphql.api.PaymentMethod;
import com.example.graphql.api.User;
import com.example.graphql.api.UserResolver;
import com.example.graphql.model.PaymentMethodImpl;
import graphql.schema.DataFetchingEnvironment;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLModelResolver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Alexey Zhokhov
 */
@GraphQLModelResolver(User.class)
public class UserResolverImpl implements UserResolver {

    public CompletionStage<List<PaymentMethod>> paymentMethodList(User user, DataFetchingEnvironment env) {
        return CompletableFuture.completedFuture(
                Arrays.asList(
                        new PaymentMethodImpl("123"),
                        new PaymentMethodImpl("456")
                )
        );
    }

}
