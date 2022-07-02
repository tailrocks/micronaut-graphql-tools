package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import org.intellij.lang.annotations.Language

class ScalarsExtendedAsFieldsSpec extends AbstractTest {

    static final String SPEC_NAME = "ScalarsSpec2"

    void "test mapping extended graphql-java scalars in root resolver"() {
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
    testLong1
    testLong2
    testShort1
    testShort2
    testBigDecimal
    testBigInteger
}
""")

        then:
            result.errors.isEmpty()
            result.data.testLong1 == Long.MAX_VALUE
            result.data.testLong2 == Long.MIN_VALUE
            result.data.testShort1 == Short.MAX_VALUE
            result.data.testShort2 == Short.MIN_VALUE
            result.data.testBigDecimal == BigDecimal.ZERO
            result.data.testBigInteger == BigInteger.ONE
    }

    void "test mapping extended graphql-java scalars in root resolver [required field]"() {
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
    testLong1
    testLong2
    testShort1
    testShort2
    testBigDecimal
    testBigInteger
}
""")

        then:
            result.errors.isEmpty()
            result.data.testLong1 == Long.MAX_VALUE
            result.data.testLong2 == Long.MIN_VALUE
            result.data.testShort1 == Short.MAX_VALUE
            result.data.testShort2 == Short.MIN_VALUE
            result.data.testBigDecimal == BigDecimal.ZERO
            result.data.testBigInteger == BigInteger.ONE
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        long testLong1() {
            return Long.MAX_VALUE
        }

        Long testLong2() {
            return Long.MIN_VALUE
        }

        short testShort1() {
            return Short.MAX_VALUE
        }

        Short testShort2() {
            return Short.MIN_VALUE
        }

        BigDecimal testBigDecimal() {
            return BigDecimal.ZERO
        }

        BigInteger testBigInteger() {
            return BigInteger.ONE
        }
    }

}
