package com.example.graphql.resolver;

import com.example.graphql.model.CatalogSection;
import com.example.graphql.model.Image;
import graphql.schema.DataFetchingEnvironment;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLModelResolver;
import io.micronaut.core.annotation.Nullable;

/**
 * @author Alexey Zhokhov
 */
@GraphQLModelResolver(CatalogSection.class)
public class CatalogSectionResolver {

    @Nullable
    public Image icon(CatalogSection catalogSection, DataFetchingEnvironment env) {
        if (catalogSection.getSlug().equals("abc")) {
            return new Image("http://google.com", 150, 100);
        }
        return null;
    }

}
