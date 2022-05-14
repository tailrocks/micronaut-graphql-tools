package io.micronaut.graphql.tools;

import static java.text.MessageFormat.format;

public class MethodNotFoundException extends RuntimeException {

    private final String methodName;

    public MethodNotFoundException(String methodName) {
        super(format("The method `{0}` not found in any GraphQL query resolvers", methodName));
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

}
