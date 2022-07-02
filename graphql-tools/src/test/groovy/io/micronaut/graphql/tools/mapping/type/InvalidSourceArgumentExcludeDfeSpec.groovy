package io.micronaut.graphql.tools.mapping.type

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import io.micronaut.graphql.tools.exceptions.InvalidSourceArgumentException
import org.intellij.lang.annotations.Language

class InvalidSourceArgumentExcludeDfeSpec extends AbstractTest {

    static final String SPEC_NAME = "InvalidSourceArgumentExcludeDfeSpec"

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

    void "test first argument in GraphQlType method is source instance (exclude DataFetchingEnvironment)"() {
        when:
            startContext(SCHEMA, SPEC_NAME)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof InvalidSourceArgumentException
            e.cause.message == """The source argument must be instance of ${User.name} class, provided: ${String.name}.
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${UserResolver.name}
  Mapped method: username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == UserResolver
            e.cause.mappingDetails.mappedMethod == "username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedClass == String
            e.cause.requiredClass == User
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query2 {
        User user() {
            return null
        }
    }

    @GraphQLType
    static class User {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(User.class)
    static class UserResolver {
        String username(String uid, DataFetchingEnvironment dfe) {
            return null
        }
    }

}