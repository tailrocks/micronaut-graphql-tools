package io.micronaut.graphql.tools.mapping.type

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLField
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.exceptions.MultipleMethodsFoundException
import org.intellij.lang.annotations.Language

class MultipleMethodsFoundSpec extends AbstractTest {

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
            getGraphQLBean()

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MultipleMethodsFoundException
            e.cause.message == """Found multiple methods for one GraphQL field.
  GraphQL type: User
  GraphQL field: username
  Methods: 
  1) ${User.name} getUsername()
  2) ${User.name} username()"""
            e.cause.mappingContext.graphQlType == 'User'
            e.cause.mappingContext.graphQlField == 'username'
            e.cause.mappingContext.mappedClass == null
            e.cause.mappingContext.mappedMethod == null
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

        @GraphQLField
        String username() {
            return username
        }
    }

}
