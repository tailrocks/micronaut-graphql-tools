package io.micronaut.graphql.tools.exceptions

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import org.intellij.lang.annotations.Language

class SchemaVerificationSpec7 extends AbstractTest {

    static final String SPEC_NAME_1 = "SchemaVerificationSpec7_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec7_2"

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

    void "test method in the type resolver has zero arguments when GraphQL schema has one"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 1, required 2 arg(s): (${SchemaVerificationSpec7.User1.name} user, ID uid)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${SchemaVerificationSpec7.User1Resolver.name}
  Mapped method: username(${SchemaVerificationSpec7.User1.name} user)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec7.User1Resolver
            e.cause.mappingDetails.mappedMethod == "username(${SchemaVerificationSpec7.User1.name} user)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 2
    }

    void "test method in the type resolver has zero arguments (exclude DataFetchingEnvironment) when GraphQL schema has one"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 1, required 2 arg(s): (${SchemaVerificationSpec7.User2.name} user, ID uid)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${SchemaVerificationSpec7.User2Resolver.name}
  Mapped method: username(${SchemaVerificationSpec7.User2.name} user, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec7.User2Resolver
            e.cause.mappingDetails.mappedMethod == "username(${SchemaVerificationSpec7.User2.name} user, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 2
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        SchemaVerificationSpec7.User1 user() {
            return null
        }
    }

    @GraphQLType
    static class User1 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLTypeResolver(SchemaVerificationSpec7.User1.class)
    static class User1Resolver {
        String username(SchemaVerificationSpec7.User1 user) {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        SchemaVerificationSpec7.User2 user() {
            return null
        }
    }

    @GraphQLType
    static class User2 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLTypeResolver(SchemaVerificationSpec7.User2.class)
    static class User2Resolver {
        String username(SchemaVerificationSpec7.User2 user, DataFetchingEnvironment dfe) {
            return null
        }
    }

}
