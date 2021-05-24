package io.github.expatiat.micronaut.graphql.tools;

import io.micronaut.core.annotation.Nullable;

import java.util.Optional;

/**
 * @author Alexey Zhokhov
 */
class ArgumentDetails {

    static final String SOURCE_ARGUMENT = "* SOURCE *";
    static final String DATA_FETCHING_ENVIRONMENT_ARGUMENT = "* DFE *";

    private final String name;

    @Nullable
    private final Class inputClass;

    public ArgumentDetails(String name, @Nullable Class inputClass) {
        this.name = name;
        this.inputClass = inputClass;
    }

    public String getName() {
        return name;
    }

    public Optional<Class> getInputClass() {
        return Optional.ofNullable(inputClass);
    }

}
