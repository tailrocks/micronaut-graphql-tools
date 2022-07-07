package io.micronaut.graphql.tools.mapping.resolvers.type

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import org.intellij.lang.annotations.Language

class XxxSpec extends AbstractTest {

    static final String SPEC_NAME = "mapping.resolvers.type.XxxSpec"

    @Language("GraphQL")
    static String SCHEMA = """
schema {
  query: Query
}

type Query {
  user: User
}

type User {
  username: String!
  roles: [String!]!
}
"""

    void "test TODO"() {
        given:
            startContext(SCHEMA, SPEC_NAME)

        when:
            def result = executeQuery("""
{ 
    user {
        username
        roles
    }
}
""")

        then:
            result.errors.isEmpty()
            result.dataPresent
            result.data.user.username == 'test'
            result.data.user.paymentMethodList.size() == 2
            result.data.user.paymentMethodList[0].number == '123'
            result.data.user.paymentMethodList[1].number == '456'
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return new User(username: 'test')
        }
    }

    @GraphQLType
    static class User {
        String username
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(User.class)
    static class UserResolver {
        List<String> roles(User user) {
            return Collections.emptyList()
        }

        List<String> roles(User user, DataFetchingEnvironment env) {
            return Collections.emptyList()
        }
    }

}
