package com.example.graphql.model;

import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLModel;

import java.util.List;

/**
 * @author Alexey Zhokhov
 */
@GraphQLModel
public class CatalogSectionListPayload {

    private final List<CatalogSection> data;

    public CatalogSectionListPayload(List<CatalogSection> data) {
        this.data = data;
    }

    public List<CatalogSection> getData() {
        return data;
    }

}
