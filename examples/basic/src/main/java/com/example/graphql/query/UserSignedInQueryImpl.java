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
package com.example.graphql.query;

import com.example.graphql.api.User;
import com.example.graphql.api.UserSignedInQuery;
import com.example.graphql.model.UserImpl;
import graphql.schema.DataFetchingEnvironment;
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver;
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
