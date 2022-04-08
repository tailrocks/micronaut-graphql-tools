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
package io.micronaut.graphql.tools.model;

import graphql.schema.DataFetchingEnvironment;
import io.micronaut.graphql.tools.annotation.GraphQLParameterized;
import io.micronaut.graphql.tools.annotation.GraphQLType;
import io.micronaut.graphql.tools.input.Limit;

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
        return description.substring(0, maxLength);
    }

    @GraphQLParameterized
    public String overview(String prefix, Limit limit) {
        if (overview != null) {
            return (prefix != null ? prefix : "") + overview.substring(limit.getStart(), limit.getEnd());
        }
        return null;
    }

}
