package com.example.graphql.model;

import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLType;

/**
 * @author Alexey Zhokhov
 */
@GraphQLType(PayloadError.class)
public class SecurityError implements PayloadError {

    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
