/*
 * Copyright 2021 original authors
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
package com.example.graphql.query;

import com.example.graphql.model.PayloadError;
import com.example.graphql.model.SecurityError;
import com.example.graphql.model.ValidationError;
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver;

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
