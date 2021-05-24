package io.github.expatiat.micronaut.graphql.tools;

/**
 * @author Alexey Zhokhov
 */
public final class BeanIntrospectionUtils {

    private BeanIntrospectionUtils() {
    }

    public static String generateGetMethodName(String name) {
        return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}
