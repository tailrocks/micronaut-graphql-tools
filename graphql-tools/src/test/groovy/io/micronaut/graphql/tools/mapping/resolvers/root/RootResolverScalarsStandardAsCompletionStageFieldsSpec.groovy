package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import org.intellij.lang.annotations.Language

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class RootResolverScalarsStandardAsCompletionStageFieldsSpec extends AbstractTest {

    static final String SPEC_NAME = "RootResolverScalarsStandardAsCompletionStageFieldsSpec"

    void "test mapping standard graphql scalars in root resolver"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  testString: String
  testBoolean: Boolean
  testInt: Int
  testFloat: Float
  testID: ID
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    testString
    testBoolean
    testInt
    testFloat
    testID
}
""")

        then:
            result.errors.isEmpty()
            result.data.testString == 'test'
            result.data.testBoolean == false
            result.data.testInt == Integer.MIN_VALUE
            result.data.testFloat == Float.MIN_VALUE as Double
            result.data.testID == 'id'
    }

    void "test mapping standard graphql scalars in root resolver [required fields]"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  testString: String!
  testBoolean: Boolean!
  testInt: Int!
  testFloat: Float!
  testID: ID!
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    testString
    testBoolean
    testInt
    testFloat
    testID
}
""")

        then:
            result.errors.isEmpty()
            result.data.testString == 'test'
            result.data.testBoolean == false
            result.data.testInt == Integer.MIN_VALUE
            result.data.testFloat == Float.MIN_VALUE as Double
            result.data.testID == 'id'
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        CompletionStage<String> testString() {
            return CompletableFuture.completedFuture("test")
        }

        CompletionStage<Boolean> testBoolean() {
            return CompletableFuture.completedFuture(Boolean.FALSE)
        }

        CompletionStage<Integer> testInt() {
            return CompletableFuture.completedFuture(Integer.valueOf(Integer.MIN_VALUE))
        }

        CompletionStage<Float> testFloat() {
            return CompletableFuture.completedFuture(Float.valueOf(Float.MIN_VALUE))
        }

        CompletionStage<String> testID() {
            return CompletableFuture.completedFuture("id")
        }
    }

}
