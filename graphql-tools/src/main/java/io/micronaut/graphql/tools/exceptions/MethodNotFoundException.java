package io.micronaut.graphql.tools.exceptions;

public class MethodNotFoundException extends RuntimeException {

    private final String methodName;

    public MethodNotFoundException(String methodName) {
        super(String.format("The method `%s` not found in any GraphQL query resolvers.", methodName));
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

}
