/*
 * Copyright 2021-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.graphql.model.resolver;

import com.example.graphql.api.PaymentMethod;
import com.example.graphql.api.User;
import com.example.graphql.api.UserResolver;
import com.example.graphql.model.PaymentMethodImpl;
import graphql.schema.DataFetchingEnvironment;
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Alexey Zhokhov
 */
@GraphQLTypeResolver(User.class)
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
