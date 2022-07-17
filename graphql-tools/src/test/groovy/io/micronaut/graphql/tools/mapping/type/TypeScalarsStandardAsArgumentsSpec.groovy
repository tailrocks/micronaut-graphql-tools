package io.micronaut.graphql.tools.mapping.type

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLField
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import org.intellij.lang.annotations.Language

class TypeScalarsStandardAsArgumentsSpec extends AbstractTest {

    static final String SPEC_NAME = "TypeScalarsStandardAsArgumentsSpec"

    void "mapping standard graphql scalars as inputs in GraphQLType annotated entity"() {
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
  hello(
    testString: String
    testBoolean1: Boolean
    testBoolean2: Boolean
    testInt1: Int
    testInt2: Int
    testFloat1: Float
    testFloat2: Float
    testID: ID
  ): String
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    test {
        hello(
            testString: "test",
            testBoolean1: true,
            testBoolean2: false,
            testInt1: 123,
            testInt2: -123,
            testFloat1: 1.23,
            testFloat2: -1.23,
            testID: "id"
        )
    }
}
""")

        then:
            result.errors.isEmpty()
            result.data.test.hello == 'World'
    }

    void "mapping standard graphql scalars as inputs in GraphQLType annotated entity [required field]"() {
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
  hello(
    testString: String!
    testBoolean1: Boolean!
    testBoolean2: Boolean!
    testInt1: Int!
    testInt2: Int!
    testFloat1: Float!
    testFloat2: Float!
    testID: ID!
  ): String
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    test {
        hello(
            testString: "test",
            testBoolean1: true,
            testBoolean2: false,
            testInt1: 123,
            testInt2: -123,
            testFloat1: 1.23,
            testFloat2: -1.23,
            testID: "id"
        )
    }
}
""")

        then:
            result.errors.isEmpty()
            result.data.test.hello == 'World'
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
        @GraphQLField
        String hello(
                String testString,
                boolean testBoolean1,
                Boolean testBoolean2,
                int testInt1,
                Integer testInt2,
                float testFloat1,
                Float testFloat2,
                String testID
        ) {
            assert testString == 'test'
            assert testBoolean1 == true
            assert testBoolean2 == false
            assert testInt1 == 123
            assert testInt2 == -123
            assert testFloat1 == 1.23f
            assert testFloat2 == -1.23f
            assert testID == 'id'

            return "World"
        }
    }

}
