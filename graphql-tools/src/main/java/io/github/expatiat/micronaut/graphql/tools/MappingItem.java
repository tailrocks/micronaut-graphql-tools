package io.github.expatiat.micronaut.graphql.tools;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.util.ArgumentUtils;

/**
 * @author Alexey Zhokhov
 */
public class MappingItem {

    @Nullable
    private final BeanIntrospection beanIntrospection;

    @Nullable
    private final Class targetEnum;

    public MappingItem(@NonNull BeanIntrospection beanIntrospection) {
        ArgumentUtils.requireNonNull("beanIntrospection", beanIntrospection);
        this.beanIntrospection = beanIntrospection;
        this.targetEnum = null;
    }

    public MappingItem(@NonNull Class targetEnum) {
        ArgumentUtils.requireNonNull("targetEnum", targetEnum);
        this.beanIntrospection = null;
        this.targetEnum = targetEnum;
    }

    @Nullable
    public BeanIntrospection getBeanIntrospection() {
        return beanIntrospection;
    }

    @Nullable
    public Class getTargetEnum() {
        return targetEnum;
    }

}
