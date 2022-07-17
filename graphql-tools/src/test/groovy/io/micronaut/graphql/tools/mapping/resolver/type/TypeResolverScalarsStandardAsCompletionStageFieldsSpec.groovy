package io.micronaut.graphql.tools.mapping.resolver.type

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import org.intellij.lang.annotations.Language

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class TypeResolverScalarsStandardAsCompletionStageFieldsSpec extends AbstractTest {

    static final String SPEC_NAME = "TypeResolverScalarsStandardAsCompletionStageFieldsSpec"

    void "test mapping standard graphql scalars in type resolver"() {
        given:
            @Language("GraphQL")
            String schema = """
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
    }

    void "test mapping standard graphql scalars in type resolver [required fields]"() {
        given:
            @Language("GraphQL")
            String schema = """
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
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(Test.class)
    static class TestResolver {
        CompletionStage<String> testString(Test test) {
            return CompletableFuture.completedFuture("test")
        }

        CompletionStage<Boolean> testBoolean(Test test) {
            return CompletableFuture.completedFuture(Boolean.FALSE)
        }

        CompletionStage<Integer> testInt(Test test) {
            return CompletableFuture.completedFuture(Integer.valueOf(Integer.MIN_VALUE))
        }

        CompletionStage<Float> testFloat(Test test) {
            return CompletableFuture.completedFuture(Float.valueOf(Float.MIN_VALUE))
        }

        CompletionStage<String> testID(Test test) {
            return CompletableFuture.completedFuture("id")
        }
    }

}
