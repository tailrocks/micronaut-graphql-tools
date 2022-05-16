package io.micronaut.graphql.tools

import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import jakarta.inject.Singleton
import org.intellij.lang.annotations.Language

import static java.util.Arrays.asList

class DataFetchingEnvironmentSpec extends AbstractTest {

    void "test DataFetchingEnvironment passed to Query's field"() {
        given:
            startContext(SCHEMA)

        when:
            def result = executeQuery("""
{ 
    userSignedIn {
        email
    }
}
""")

        then:
            result.errors.isEmpty()
            result.dataPresent
            result.data.userSignedIn.email == 'me@test.com'
    }

    void "test DataFetchingEnvironment passed to Resolver's field"() {
        given:
            startContext(SCHEMA)

        when:
            def result = executeQuery("""
{ 
    userSignedIn {
        paymentMethodList {
            number
        }
    }
}
""")

        then:
            result.errors.isEmpty()
            result.dataPresent
            result.data.userSignedIn.paymentMethodList.size() == 2
            result.data.userSignedIn.paymentMethodList[0].number == '123'
            result.data.userSignedIn.paymentMethodList[1].number == '456'
    }

    @Language("GraphQL")
    static String SCHEMA = """
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

    @Requires(property = 'spec.name', value = 'DataFetchingEnvironmentSpec')
    @GraphQLRootResolver
    static class Query {
        User userSignedIn(DataFetchingEnvironment env) {
            if (env == null) {
                throw new NullPointerException()
            }
            return new User('me@test.com')
        }
    }

    @Requires(property = 'spec.name', value = 'DataFetchingEnvironmentSpec')
    @GraphQLTypeResolver(User.class)
    static class UserResolver {
        List<PaymentMethod> paymentMethodList(User user, DataFetchingEnvironment env) {
            if (user == null) {
                throw new NullPointerException()
            }
            if (env == null) {
                throw new NullPointerException()
            }
            return asList(
                    new PaymentMethod('123'),
                    new PaymentMethod('456')
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
