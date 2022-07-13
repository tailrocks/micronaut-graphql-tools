package io.micronaut.graphql.tools;

import io.micronaut.core.annotation.Nullable;

public interface MappingContext {

    @Nullable
    Class<?> getMappedClass();

    String getMessage(String superMessage);

}
