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
package com.example.graphql.model;

import com.example.graphql.api.User;
import io.micronaut.graphql.tools.annotation.GraphQLType;
import io.micronaut.core.annotation.NonNull;

/**
 * @author Alexey Zhokhov
 */
@GraphQLType(User.class)
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
