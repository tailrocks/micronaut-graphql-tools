package com.example.graphql.model;

import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLModel;

/**
 * @author Alexey Zhokhov
 */
@GraphQLModel
public class Image {

    private final String url;
    private final int width;
    private final int height;

    public Image(String url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
