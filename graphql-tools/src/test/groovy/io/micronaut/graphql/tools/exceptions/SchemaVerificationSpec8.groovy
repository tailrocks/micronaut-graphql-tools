package io.micronaut.graphql.tools.exceptions

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import org.intellij.lang.annotations.Language

class SchemaVerificationSpec8 extends AbstractTest {

    static final String SPEC_NAME_1 = "SchemaVerificationSpec8_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec8_2"

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

    void "test method in the type resolver has one argument when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 2, required 1 arg(s): (${SchemaVerificationSpec8.User1.name} user)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${SchemaVerificationSpec8.User1Resolver.name}
  Mapped method: username(${SchemaVerificationSpec8.User1.name} user, ${String.name} uid)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec8.User1Resolver
            e.cause.mappingDetails.mappedMethod == "username(${SchemaVerificationSpec8.User1.name} user, ${String.name} uid)"
            e.cause.providedCount == 2
            e.cause.requiredCount == 1
    }

    void "test method in the type resolver has one argument (exclude DataFetchingEnvironment) when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 2, required 1 arg(s): (${SchemaVerificationSpec8.User2.name} user)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${SchemaVerificationSpec8.User2Resolver.name}
  Mapped method: username(${SchemaVerificationSpec8.User2.name} user, ${String.name} uid, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec8.User2Resolver
            e.cause.mappingDetails.mappedMethod == "username(${SchemaVerificationSpec8.User2.name} user, ${String.name} uid, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedCount == 2
            e.cause.requiredCount == 1
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        SchemaVerificationSpec8.User1 user() {
            return null
        }
    }

    @GraphQLType
    static class User1 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLTypeResolver(SchemaVerificationSpec8.User1.class)
    static class User1Resolver {
        String username(SchemaVerificationSpec8.User1 user, String uid) {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        SchemaVerificationSpec8.User2 user() {
            return null
        }
    }

    @GraphQLType
    static class User2 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLTypeResolver(SchemaVerificationSpec8.User2.class)
    static class User2Resolver {
        String username(SchemaVerificationSpec8.User2 user, String uid, DataFetchingEnvironment dfe) {
            return null
        }
    }

}
