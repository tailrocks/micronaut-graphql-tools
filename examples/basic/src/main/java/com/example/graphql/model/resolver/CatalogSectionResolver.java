/*
 * Copyright 2021 original authors
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
package com.example.graphql.model.resolver;

import com.example.graphql.model.CatalogSection;
import com.example.graphql.model.Image;
import graphql.schema.DataFetchingEnvironment;
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver;
import io.micronaut.core.annotation.Nullable;

/**
 * @author Alexey Zhokhov
 */
@GraphQLTypeResolver(CatalogSection.class)
public class CatalogSectionResolver {

    @Nullable
    public Image icon(CatalogSection catalogSection, DataFetchingEnvironment env) {
        if (catalogSection.getSlug().equals("abc")) {
            return new Image("http://google.com", 150, 100);
        }
        return null;
    }

}
