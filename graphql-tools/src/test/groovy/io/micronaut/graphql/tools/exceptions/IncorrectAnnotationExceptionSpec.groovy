package io.micronaut.graphql.tools.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanContextException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import org.intellij.lang.annotations.Language

class IncorrectAnnotationExceptionSpec extends AbstractTest {

    static final String SPEC_NAME = "IncorrectAnnotationExceptionSpec"

    void "test GraphQLTypeResolver annotation used with null value class"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  user: User
}

type User {
  username: String!
  gravatar: String
}
"""

        when:
            startContext(schema, SPEC_NAME)

        then:
            def e = thrown(BeanContextException)
            e.cause instanceof IncorrectAnnotationException
            e.cause.message == "Empty value member for @GraphQLTypeResolver annotation in ${UserResolver.class.name} class."
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return null
        }
    }

    @GraphQLType
    static class User {
        String getUsername() {
            return "abc"
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver
    static class UserResolver {
        String gravatar(User user) {
            return null
        }
    }

}
