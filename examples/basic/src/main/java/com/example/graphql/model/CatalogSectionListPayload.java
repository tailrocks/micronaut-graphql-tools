package com.example.graphql.model;

import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLType;

import java.util.List;

/**
 * @author Alexey Zhokhov
 */
@GraphQLType
public class CatalogSectionListPayload {

    private final List<CatalogSection> data;

    public CatalogSectionListPayload(List<CatalogSection> data) {
        this.data = data;
    }

    public List<CatalogSection> getData() {
        return data;
    }

}
