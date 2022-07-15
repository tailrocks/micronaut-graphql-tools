package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import org.intellij.lang.annotations.Language

class RootResolverSkipProcessOneTypeTwiceSpec extends AbstractTest {

    static final String SPEC_NAME = "RootResolverScalarsStandardAsFieldsSpec"

    void "test TODO"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  testUser1: User
  testUser2: User
}

type User {
  username: String!
}
"""

            startContext(schema, SPEC_NAME)

        when:
            getGraphQLBean()

        then:
            noExceptionThrown()
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User testUser1() {
            return null
        }

        User testUser2() {
            return null
        }
    }

    @GraphQLType
    static class User {
        String username
    }

}
