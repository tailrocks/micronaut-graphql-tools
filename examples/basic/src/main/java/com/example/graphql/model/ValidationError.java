package com.example.graphql.model;

import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLType;

/**
 * @author Alexey Zhokhov
 */
@GraphQLType(PayloadError.class)
public class ValidationError implements PayloadError {

    private Integer code;

    public ValidationError(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

}
