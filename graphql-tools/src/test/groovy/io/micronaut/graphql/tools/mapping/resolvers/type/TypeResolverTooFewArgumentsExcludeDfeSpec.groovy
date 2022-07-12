package io.micronaut.graphql.tools.mapping.resolvers.type

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import io.micronaut.graphql.tools.exceptions.IncorrectArgumentCountException
import org.intellij.lang.annotations.Language

class TypeResolverTooFewArgumentsExcludeDfeSpec extends AbstractTest {

    static final String SPEC_NAME = "TypeResolverTooFewArgumentsExcludeDfeSpec"

    @Language("GraphQL")
    static final String SCHEMA = """
schema {
  query: Query
}

type Query {
  user: User
}

type User {
  username(uid: ID): String
}
"""

    void "test method in the type resolver has zero arguments (exclude DataFetchingEnvironment) when GraphQL schema has one"() {
        given:
            startContext(SCHEMA, SPEC_NAME)

        when:
            getGraphQLBean()

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 1, required 2 arg(s): username(${User.name} user, ID uid)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${UserResolver.name}
  Mapped method: username(${User.name} user, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.mappingContext.graphQlType == 'User'
            e.cause.mappingContext.graphQlField == 'username'
            e.cause.mappingContext.mappedClass == UserResolver
            e.cause.mappingContext.mappedMethod == "username(${User.name} user, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 2
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
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(User.class)
    static class UserResolver {
        String username(User user, DataFetchingEnvironment dfe) {
            return null
        }
    }

}
