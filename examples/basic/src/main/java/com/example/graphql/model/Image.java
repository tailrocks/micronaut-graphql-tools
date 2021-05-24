package com.example.graphql.model;

import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLModel;

/**
 * @author Alexey Zhokhov
 */
@GraphQLModel
public class Image {

    private final String url;
    private final Integer width;
    private final Integer height;

    public Image(String url, Integer width, Integer height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

}
