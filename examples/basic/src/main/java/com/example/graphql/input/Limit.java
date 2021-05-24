package com.example.graphql.input;

import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLInput;

/**
 * @author Alexey Zhokhov
 */
@GraphQLInput
public class Limit {

    private Integer start;
    private Integer end;

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

}
