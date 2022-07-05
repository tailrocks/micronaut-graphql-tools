package io.micronaut.graphql.tools

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.annotation.GraphQLParameterized
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import org.intellij.lang.annotations.Language

import static java.util.Arrays.asList

// TODO split into three tests
class DataFetchingEnvironmentSpec extends AbstractTest {

    static final String SPEC_NAME = "DataFetchingEnvironmentSpec"

    void "test DataFetchingEnvironment passed to GraphQLRootResolver's method"() {
        given:
            startContext(SCHEMA, SPEC_NAME)

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

    void "test DataFetchingEnvironment passed to GraphQLType's method"() {
        given:
            startContext(SCHEMA, SPEC_NAME)

        when:
            def result = executeQuery("""
{ 
    userSignedIn {
        username
    }
}
""")

        then:
            result.errors.isEmpty()
            result.dataPresent
            result.data.userSignedIn.username == 'test'
    }

    void "test DataFetchingEnvironment passed to GraphQLTypeResolver's method"() {
        given:
            startContext(SCHEMA, SPEC_NAME)

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
  username: String!
  paymentMethodList: [PaymentMethod!]!
}

type PaymentMethod {
  number: String!
}
"""

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User userSignedIn(DataFetchingEnvironment env) {
            assert env != null
            assert env.field.name == 'userSignedIn'
            assert env.parentType.name == 'Query'
            return new User('me@test.com', 'test')
        }
    }

    @GraphQLType
    static class User {
        private final String email
        private final String username

        User(String email, String username) {
            this.email = email
            this.username = username
        }

        String getEmail() {
            return email
        }

        @GraphQLParameterized
        String getUsername(DataFetchingEnvironment env) {
            assert env != null
            assert env.field.name == 'username'
            assert env.parentType.name == 'User'
            return username
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(User.class)
    static class UserResolver {
        List<PaymentMethod> paymentMethodList(User user, DataFetchingEnvironment env) {
            assert env != null
            assert env.field.name == 'paymentMethodList'
            assert env.parentType.name == 'User'
            return asList(
                    new PaymentMethod('123'),
                    new PaymentMethod('456')
            )
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
