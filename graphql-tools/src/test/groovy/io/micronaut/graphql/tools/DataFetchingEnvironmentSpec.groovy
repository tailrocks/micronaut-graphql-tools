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
import org.intellij.lang.annotations.Language

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class DataFetchingEnvironmentSpec extends AbstractTest {

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
            executionResult.data.userSignedIn.email == "me@test.com"
    }

    void "test DataFetchingEnvironment passed to Resolver's field"() {
        given:
            startContext()

        when:
            def executionResult = applicationContext.getBean(GraphQL.class).execute("""
{ 
    userSignedIn {
        paymentMethodList {
            number
        }
    }
}
""")

        then:
            executionResult.errors.isEmpty()
            executionResult.dataPresent
            executionResult.data.userSignedIn.paymentMethodList.size() == 2
            executionResult.data.userSignedIn.paymentMethodList[0].number == "123"
            executionResult.data.userSignedIn.paymentMethodList[1].number == "456"
    }

    @Requires(property = "spec.name", value = "DataFetchingEnvironmentSpec")
    @Factory
    static class TypeDefinitionRegistryFactory {

        @Bean
        @Singleton
        TypeDefinitionRegistry typeDefinitionRegistry() {
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  userSignedIn: User
}

type User {
  email: String!
  paymentMethodList: [PaymentMethod!]!
}

type PaymentMethod {
  number: String!
}
"""

            TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry()
            typeRegistry.merge(new SchemaParser().parse(schema))

            return typeRegistry
        }

    }

    @Requires(property = "spec.name", value = "DataFetchingEnvironmentSpec")
    @GraphQLRootResolver
    static class Query {
        User userSignedIn(DataFetchingEnvironment env) {
            if (env == null) {
                throw new NullPointerException()
            }
            return new User("me@test.com")
        }
    }

    @Requires(property = "spec.name", value = "DataFetchingEnvironmentSpec")
    @GraphQLTypeResolver(User.class)
    static class UserResolver {
        List<PaymentMethod> paymentMethodList(User user, DataFetchingEnvironment env) {
            if (user == null) {
                throw new NullPointerException()
            }
            if (env == null) {
                throw new NullPointerException()
            }
            return Arrays.asList(
                    new PaymentMethod("123"),
                    new PaymentMethod("456")
            )
        }
    }

    @GraphQLType
    static class User {
        private final String email

        User(String email) {
            this.email = email
        }

        String getEmail() {
            return email
        }
    }

    @GraphQLType
    static class PaymentMethod {
        private final String number

        PaymentMethod(String number) {
            this.number = number
        }

        String getNumber() {
            return number
        }
    }

}
