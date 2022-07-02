package io.micronaut.graphql.tools.mapping.resolvers.root

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.exceptions.IncorrectArgumentCountException
import org.intellij.lang.annotations.Language

class TooManyArgumentsExcludeDfeSpec extends AbstractTest {

    static final String SPEC_NAME = "TooManyArgumentsExcludeDfeSpec"

    @Language("GraphQL")
    static final String SCHEMA = """
schema {
  query: Query
}

type Query {
  username: String
}
"""

    void "test method in the root resolver has one argument (exclude DataFetchingEnvironment) when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 1, required 0 arg(s).
  GraphQL type: Query
  GraphQL field: username
  Mapped class: ${Query2.name}
  Mapped method: username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == Query2
            e.cause.mappingDetails.mappedMethod == "username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 0
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query2 {
        String username(String uid, DataFetchingEnvironment dfe) {
            return null
        }
    }

}