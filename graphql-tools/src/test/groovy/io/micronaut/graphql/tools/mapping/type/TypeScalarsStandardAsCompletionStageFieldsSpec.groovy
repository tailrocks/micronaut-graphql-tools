package io.micronaut.graphql.tools.mapping.type

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import org.intellij.lang.annotations.Language
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class TypeScalarsStandardAsCompletionStageFieldsSpec extends AbstractTest {

    static final String SPEC_NAME = "TypeScalarsStandardAsCompletionStageFieldsSpec"

    @Language("GraphQL")
    static final String SCHEMA1 = """
schema {
  query: Query
}

type Query {
  test: Test
}

type Test {
  testString: String
  testBoolean: Boolean
  testInt: Int
  testFloat: Float
  testID: ID
}
"""

    @Language("GraphQL")
    static final String SCHEMA2 = """
schema {
  query: Query
}

type Query {
  test: Test
}

type Test {
  testString: String!
  testBoolean: Boolean!
  testInt: Int!
  testFloat: Float!
  testID: ID!
}
"""

    @Unroll
    void "use standard graphql scalars as method return types wrapped with CompletionStage"() {
        given:
            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    test {
        testString
        testBoolean
        testInt
        testFloat
        testID
    }
}
""")

        then:
            result.errors.isEmpty()
            result.data.test.testString == 'test'
            result.data.test.testBoolean == false
            result.data.test.testInt == Integer.MIN_VALUE
            result.data.test.testFloat == Float.MIN_VALUE as Double
            result.data.test.testID == 'id'

        where:
            schema << [SCHEMA1, SCHEMA2]
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        Test test() {
            return new Test()
        }
    }

    @GraphQLType
    static class Test {
        CompletionStage<String> testString = CompletableFuture.completedFuture("test")
        CompletionStage<Boolean> testBoolean = CompletableFuture.completedFuture(Boolean.FALSE)
        CompletionStage<Integer> testInt = CompletableFuture.completedFuture(Integer.valueOf(Integer.MIN_VALUE))
        CompletionStage<Float> testFloat = CompletableFuture.completedFuture(Float.valueOf(Float.MIN_VALUE))
        CompletionStage<String> testID = CompletableFuture.completedFuture("id")
    }

}
