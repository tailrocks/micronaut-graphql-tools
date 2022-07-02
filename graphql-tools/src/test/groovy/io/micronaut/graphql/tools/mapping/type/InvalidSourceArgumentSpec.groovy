package io.micronaut.graphql.tools.mapping.type

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import io.micronaut.graphql.tools.exceptions.InvalidSourceArgumentException
import org.intellij.lang.annotations.Language

class InvalidSourceArgumentSpec extends AbstractTest {

    static final String SPEC_NAME = "mapping.type.InvalidSourceArgumentSpec"

    @Language("GraphQL")
    static final String SCHEMA = """
schema {
  query: Query
}

type Query {
  user: User
}

type User {
  username: String
}
"""

    void "test first argument in GraphQlType method is source instance"() {
        when:
            startContext(SCHEMA, SPEC_NAME)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof InvalidSourceArgumentException
            e.cause.message == """The source argument must be instance of ${User.name} class, provided: ${String.name}.
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User1Resolver.name}
  Mapped method: username(${String.name} uid)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == User1Resolver
            e.cause.mappingDetails.mappedMethod == "username(${String.name} uid)"
            e.cause.providedClass == String
            e.cause.requiredClass == User
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query1 {
        User user() {
            return null
        }
    }

    @GraphQLType
    static class User {
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(User.class)
    static class User1Resolver {
        String username(String uid) {
            return null
        }
    }

}
