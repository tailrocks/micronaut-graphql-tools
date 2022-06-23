package io.micronaut.graphql.tools.exceptions

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import org.intellij.lang.annotations.Language

class InvalidSourceArgumentExceptionSpec1 extends AbstractTest {

    static final String SPEC_NAME_1 = "SchemaVerificationSpec9_1"

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
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof InvalidSourceArgumentException
            e.cause.message == """The source argument must be instance of ${User1.name} class, provided: ${String.name}.
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User1Resolver.name}
  Mapped method: username(${String.name} uid)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == User1Resolver
            e.cause.mappingDetails.mappedMethod == "username(${String.name} uid)"
            e.cause.providedClass == String
            e.cause.requiredClass == User1
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        User1 user() {
            return null
        }
    }

    @GraphQLType
    static class User1 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLTypeResolver(User1.class)
    static class User1Resolver {
        String username(String uid) {
            return null
        }
    }

}

class InvalidSourceArgumentExceptionSpec2 extends AbstractTest {

    static final String SPEC_NAME_2 = "SchemaVerificationSpec9_2"

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
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof InvalidSourceArgumentException
            e.cause.message == """The source argument must be instance of ${User2.name} class, provided: ${String.name}.
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User2Resolver.name}
  Mapped method: username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == User2Resolver
            e.cause.mappingDetails.mappedMethod == "username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedClass == String
            e.cause.requiredClass == User2
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        User2 user() {
            return null
        }
    }

    @GraphQLType
    static class User2 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLTypeResolver(User2.class)
    static class User2Resolver {
        String username(String uid, DataFetchingEnvironment dfe) {
            return null
        }
    }

}
