package io.micronaut.graphql.tools.exceptions

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import org.intellij.lang.annotations.Language

class SchemaVerificationSpec5 extends AbstractTest {

    static final String SPEC_NAME_1 = "SchemaVerificationSpec5_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec5_2"

    @Language("GraphQL")
    static final String SCHEMA = """
schema {
  query: Query
}

type Query {
  username(uid: ID): String
}
"""

    void "test method in the root resolver has zero arguments when GraphQL schema has one"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 0, required 1 arg(s): (ID uid)
  GraphQL type: Query
  GraphQL field: username
  Mapped class: ${SchemaVerificationSpec5.Query1.name}
  Mapped method: username()"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec5.Query1
            e.cause.mappingDetails.mappedMethod == 'username()'
            e.cause.providedCount == 0
            e.cause.requiredCount == 1
    }

    void "test method in the root resolver has zero arguments (exclude DataFetchingEnvironment) when GraphQL schema has one"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 0, required 1 arg(s): (ID uid)
  GraphQL type: Query
  GraphQL field: username
  Mapped class: ${SchemaVerificationSpec5.Query2.name}
  Mapped method: username(${DataFetchingEnvironment.name} dfe)"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec5.Query2
            e.cause.mappingDetails.mappedMethod == "username(${DataFetchingEnvironment.name} dfe)"
            e.cause.providedCount == 0
            e.cause.requiredCount == 1
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        String username() {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        String username(DataFetchingEnvironment dfe) {
            return null
        }
    }

}
