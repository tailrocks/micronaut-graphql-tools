package com.example.graphql.model;

import com.example.graphql.input.Limit;
import graphql.schema.DataFetchingEnvironment;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLType;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLParameterized;

/**
 * @author Alexey Zhokhov
 */
@GraphQLType
public class CatalogSection {

    private final String slug;
    private final String name;
    private final String description;
    private final String overview;

    public CatalogSection(String slug, String name, String description, String overview) {
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.overview = overview;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    @GraphQLParameterized
    public String getDescription(Integer maxLength, DataFetchingEnvironment environment) {
        return description;
    }

    @GraphQLParameterized
    public String overview(String prefix, Limit limit) {
        if (overview != null) {
            return (prefix != null ? prefix : "") + overview;
        }
        return null;
    }

}
