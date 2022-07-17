package io.micronaut.graphql.tools.mapping.resolver.root

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLInput
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import org.intellij.lang.annotations.Language

class RootResolverScalarsExtendedAsInputValuesSpec extends AbstractTest {

    static final String SPEC_NAME = "RootResolverScalarsExtendedAsInputValuesSpec"

    void "test mapping extended graphql-java scalars as inputs in root resolver"() {
        given:
            @Language("GraphQL")
            String schema = """
scalar Long
scalar Short
scalar BigDecimal
scalar BigInteger

schema {
  query: Query
}

type Query {
  hello(input: HelloInput): String
}

input HelloInput {
  testLong1: Long
  testLong2: Long
  testShort1: Short
  testShort2: Short
  testBigDecimal: BigDecimal
  testBigInteger: BigInteger
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    hello(input: {
        testLong1: 123,
        testLong2: -123,
        testShort1: 1,
        testShort2: -1,
        testBigDecimal: 10.00,
        testBigInteger: 10
    })
}
""")

        then:
            result.errors.isEmpty()
            result.data.hello == 'World'
    }

    void "test mapping extended graphql-java scalars as inputs in root resolver [required field]"() {
        given:
            @Language("GraphQL")
            String schema = """
scalar Long
scalar Short
scalar BigDecimal
scalar BigInteger

schema {
  query: Query
}

type Query {
  hello(input: HelloInput!): String
}

input HelloInput {
  testLong1: Long!
  testLong2: Long!
  testShort1: Short!
  testShort2: Short!
  testBigDecimal: BigDecimal!
  testBigInteger: BigInteger!
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    hello(input: {
        testLong1: 123,
        testLong2: -123,
        testShort1: 1,
        testShort2: -1,
        testBigDecimal: 10.00,
        testBigInteger: 10
    })
}
""")

        then:
            result.errors.isEmpty()
            result.data.hello == 'World'
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        String hello(HelloInput input) {
            assert input.testLong1 == 123
            assert input.testLong2 == -123
            assert input.testShort1 == 1
            assert input.testShort2 == -1
            assert input.testBigDecimal == BigDecimal.valueOf(10.00)
            assert input.testBigInteger == BigInteger.valueOf(10)

            return "World"
        }
    }

    @GraphQLInput
    static class HelloInput {
        long testLong1
        Long testLong2
        short testShort1
        Short testShort2
        BigDecimal testBigDecimal
        BigInteger testBigInteger
    }

}
