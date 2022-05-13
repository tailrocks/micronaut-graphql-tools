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
package io.micronaut.graphql.tools.mutation;

import graphql.schema.DataFetchingEnvironment;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver;
import io.micronaut.graphql.tools.input.EmailValidateInput;
import io.micronaut.graphql.tools.model.EmailValidatePayload;
import io.micronaut.graphql.tools.model.EmailValidationStatus;
import io.micronaut.graphql.tools.service.EmailValidationService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@GraphQLRootResolver
public class EmailValidateMutation {

    private final EmailValidationService emailValidationService;

    public EmailValidateMutation(EmailValidationService emailValidationService) {
        this.emailValidationService = emailValidationService;
    }

    public CompletionStage<EmailValidatePayload> emailValidate(
            @NonNull EmailValidateInput input,
            DataFetchingEnvironment env
    ) {
        return CompletableFuture.completedFuture(
                new EmailValidatePayload(
                        emailValidationService.isValid(input != null ? input.getEmail() : null) ?
                                EmailValidationStatus.VALID : EmailValidationStatus.INVALID
                )
        );
    }

}