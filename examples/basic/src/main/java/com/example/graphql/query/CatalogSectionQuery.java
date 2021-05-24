package com.example.graphql.query;

import com.example.graphql.aop.CheckNotNull;
import com.example.graphql.input.CatalogSectionListOptions;
import com.example.graphql.model.CatalogSection;
import com.example.graphql.model.CatalogSectionListPayload;
import graphql.schema.DataFetchingEnvironment;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLRootResolver;
import io.micronaut.core.annotation.NonNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Alexey Zhokhov
 */
@GraphQLRootResolver
public class CatalogSectionQuery {

    @NonNull
    public CompletionStage<CatalogSectionListPayload> catalogSectionList(Integer max, CatalogSectionListOptions options,
                                                                         DataFetchingEnvironment env) {
        CatalogSection item1 = new CatalogSection("abc", "xyz", "desc", "overview");

        return CompletableFuture.completedFuture(
                new CatalogSectionListPayload(Arrays.asList(item1))
        );
    }

    @CheckNotNull
    @NonNull
    public CompletionStage<CatalogSectionListPayload> catalogSectionTopList() {
        CatalogSection item1 = new CatalogSection("abc", "ABC", null, null);
        CatalogSection item2 = new CatalogSection("xyz", "XYZ", null, null);

        return CompletableFuture.completedFuture(
                new CatalogSectionListPayload(Arrays.asList(item1, item2))
        );
    }

}
