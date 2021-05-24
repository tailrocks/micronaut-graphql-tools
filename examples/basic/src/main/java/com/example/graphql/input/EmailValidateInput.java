package com.example.graphql.input;

import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLInput;

/**
 * @author Alexey Zhokhov
 */
@GraphQLInput
public class EmailValidateInput {

    private String email;

    public EmailValidateInput() {
    }

    public EmailValidateInput(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
