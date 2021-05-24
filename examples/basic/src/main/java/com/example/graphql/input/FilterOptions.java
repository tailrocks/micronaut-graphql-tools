package com.example.graphql.input;

import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLInput;

@GraphQLInput
public class FilterOptions {

    private Integer skip;
    private Integer offset;

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

}
