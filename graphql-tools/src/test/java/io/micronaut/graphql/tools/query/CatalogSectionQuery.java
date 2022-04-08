/*
 * Copyright 2021-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.graphql.tools.query;

import graphql.schema.DataFetchingEnvironment;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver;
import io.micronaut.graphql.tools.aop.CheckNotNull;
import io.micronaut.graphql.tools.input.CatalogSectionListOptions;
import io.micronaut.graphql.tools.model.CatalogSection;
import io.micronaut.graphql.tools.model.CatalogSectionListPayload;

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
        CatalogSection item1 = new CatalogSection("abc", "ABC", "a-c items", "Aaabbbccc");
        CatalogSection item2 = new CatalogSection("xyz", "XYZ", "x-z items", "Xxxyyyzzz");

        return CompletableFuture.completedFuture(
                new CatalogSectionListPayload(Arrays.asList(item1, item2))
        );
    }

}
