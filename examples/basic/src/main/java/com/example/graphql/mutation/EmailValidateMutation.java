package com.example.graphql.mutation;

import com.example.graphql.input.EmailValidateInput;
import com.example.graphql.model.EmailValidatePayload;
import com.example.graphql.model.EmailValidationStatus;
import com.example.graphql.service.EmailValidationService;
import edu.umd.cs.findbugs.annotations.NonNull;
import graphql.schema.DataFetchingEnvironment;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLRootResolver;

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
