package com.example.graphql.input;

import com.example.graphql.model.Sort;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLInput;

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
