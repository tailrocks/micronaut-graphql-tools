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
package com.example.graphql.model;

import com.example.graphql.api.PaymentMethod;
import io.micronaut.graphql.tools.annotation.GraphQLType;

/**
 * @author Alexey Zhokhov
 */
@GraphQLType(PaymentMethod.class)
public class PaymentMethodImpl implements PaymentMethod {

    private final String number;

    public PaymentMethodImpl(String number) {
        this.number = number;
    }

    @Override
    public String getNumber() {
        return number;
    }

}
