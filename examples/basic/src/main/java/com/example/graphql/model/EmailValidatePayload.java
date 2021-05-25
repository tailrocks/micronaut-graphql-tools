package com.example.graphql.model;

import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLType;

/**
 * @author Alexey Zhokhov
 */
@GraphQLType
public class EmailValidatePayload {

    private EmailValidationStatus data;

    public EmailValidatePayload() {
    }

    public EmailValidatePayload(EmailValidationStatus data) {
        this.data = data;
    }

    public EmailValidationStatus getData() {
        return data;
    }

    public void setData(EmailValidationStatus data) {
        this.data = data;
    }

}
