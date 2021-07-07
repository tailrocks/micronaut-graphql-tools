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
package com.example.graphql.input;

import com.example.graphql.model.Sort;
import io.micronaut.graphql.tools.annotation.GraphQLInput;

/**
 * @author Alexey Zhokhov
 */
@GraphQLInput
public class CatalogSectionListOptions {

    private Sort sort;
    private FilterOptions filter;

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public FilterOptions getFilter() {
        return filter;
    }

    public void setFilter(FilterOptions filter) {
        this.filter = filter;
    }

}
