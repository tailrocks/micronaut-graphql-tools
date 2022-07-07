package io.micronaut.graphql.tools.mapping.type


import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLParameterized
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import org.intellij.lang.annotations.Language

// TODO rename me pls
class XxxSpec extends AbstractTest {

    static final String SPEC_NAME = "mapping.type.XxxSpec"

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
    }
}
""")

        then:
            result.errors.isEmpty()
            result.dataPresent
            result.data.user.username == 'test'
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return new User('test')
        }
    }

    @GraphQLType
    static class User {
        private final String username

        User(String username) {
            this.username = username
        }

        String getUsername() {
            return username
        }

        @GraphQLParameterized
        String username() {
            return username
        }
    }

}
