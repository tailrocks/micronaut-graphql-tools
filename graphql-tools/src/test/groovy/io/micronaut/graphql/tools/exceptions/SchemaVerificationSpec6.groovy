package io.micronaut.graphql.tools.exceptions

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLParameterized
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import org.intellij.lang.annotations.Language

class SchemaVerificationSpec6 extends AbstractTest {

    static final String SPEC_NAME_1 = "SchemaVerificationSpec6_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec6_2"

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

    void "test method in the model has one argument when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 1, required 0 arg(s).
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${SchemaVerificationSpec6.User1.name}
  Mapped method: username(${String.name} uid)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec6.User1
            e.cause.mappingDetails.mappedMethod == "username(${String.name} uid)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 0
    }

    void "test method in the model has one argument (exclude DataFetchingEnvironment) when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 1, required 0 arg(s).
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${SchemaVerificationSpec6.User2.name}
  Mapped method: username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec6.User2
            e.cause.mappingDetails.mappedMethod == "username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 0
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        SchemaVerificationSpec6.User1 user() {
            return null
        }
    }

    @GraphQLType
    static class User1 {
        @GraphQLParameterized
        String username(String uid) {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        SchemaVerificationSpec6.User2 user() {
            return null
        }
    }

    @GraphQLType
    static class User2 {
        @GraphQLParameterized
        String username(String uid, DataFetchingEnvironment dfe) {
            return null
        }
    }
}
