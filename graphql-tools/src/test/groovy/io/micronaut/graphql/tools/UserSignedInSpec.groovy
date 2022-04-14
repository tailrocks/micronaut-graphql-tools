package io.micronaut.graphql.tools

import graphql.GraphQL
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.annotation.Nullable
import io.micronaut.core.io.ResourceResolver
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import jakarta.inject.Singleton

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class UserSignedInSpec extends AbstractTest {

    void "test DataFetchingEnvironment passed to Query's field"() {
        given:
            startContext()

        when:
            def executionResult = applicationContext.getBean(GraphQL.class).execute("""
{ 
    userSignedIn {
        email
    }
}
""")

        then:
            executionResult.errors.isEmpty()
            executionResult.dataPresent
            executionResult.data["userSignedIn"]["email"] == "me@test.com"
    }

    @Requires(property = "spec.name", value = "UserSignedInSpec")
    @Factory
    static class CustomGraphQLFactory {

        @Bean
        @Singleton
        TypeDefinitionRegistry typeDefinitionRegistry(ResourceResolver resourceResolver) {
            SchemaParser schemaParser = new SchemaParser()

            TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry()
            typeRegistry.merge(schemaParser.parse(new BufferedReader(new InputStreamReader(
                    resourceResolver.getResourceAsStream("classpath:graphql/userSignedIn.graphqls").get()))))

            return typeRegistry
        }

    }

    static interface User {
        @NonNull
        String getEmail()
    }

    static interface PaymentMethod {
        String getNumber()
    }

    static interface UserResolver {
        CompletionStage<List<PaymentMethod>> paymentMethodList(User user, DataFetchingEnvironment env) throws Exception;
    }

    static interface UserSignedInQuery {
        @Nullable
        User userSignedIn(DataFetchingEnvironment env)
    }

    @GraphQLType(User.class)
    static class UserImpl implements User {
        private final String email

        UserImpl(String email) {
            this.email = email
        }

        @Override
        @NonNull
        String getEmail() {
            return email;
        }
    }

    @GraphQLType(PaymentMethod.class)
    static class PaymentMethodImpl implements PaymentMethod {
        private final String number

        PaymentMethodImpl(String number) {
            this.number = number
        }

        @Override
        String getNumber() {
            return number
        }
    }

    @Requires(property = "spec.name", value = "UserSignedInSpec")
    @GraphQLRootResolver
    static class UserSignedInQueryImpl implements UserSignedInQuery {
        @Override
        @Nullable
        User userSignedIn(DataFetchingEnvironment env) {
            if (env == null) {
                throw new NullPointerException()
            }
            return new UserImpl("me@test.com")
        }
    }

    @Requires(property = "spec.name", value = "UserSignedInSpec")
    @GraphQLTypeResolver(User.class)
    static class UserResolverImpl implements UserResolver {
        CompletionStage<List<PaymentMethod>> paymentMethodList(User user, DataFetchingEnvironment env) {
            return CompletableFuture.completedFuture(
                    Arrays.asList(
                            new PaymentMethodImpl("123"),
                            new PaymentMethodImpl("456")
                    )
            )
        }
    }

}
