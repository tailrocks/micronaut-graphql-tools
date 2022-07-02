package io.micronaut.graphql.tools.mapping.type

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import org.intellij.lang.annotations.Language

class ScalarsExtendedAsFieldsSpec extends AbstractTest {

    static final String SPEC_NAME = "mapping.type.ScalarsExtendedAsFieldsSpec"

    void "test mapping extended graphql-java scalars in GraphQLType annotated entity"() {
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
  test: Test
}

type Test {
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
    test {
        testLong1
        testLong2
        testShort1
        testShort2
        testBigDecimal
        testBigInteger
    }
}
""")

        then:
            result.errors.isEmpty()
            result.data.test.testLong1 == Long.MAX_VALUE
            result.data.test.testLong2 == Long.MIN_VALUE
            result.data.test.testShort1 == Short.MAX_VALUE
            result.data.test.testShort2 == Short.MIN_VALUE
            result.data.test.testBigDecimal == BigDecimal.ZERO
            result.data.test.testBigInteger == BigInteger.ONE
    }

    void "test mapping extended graphql-java scalars in GraphQLType annotated entity [required field]"() {
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
  test: Test
}

type Test {
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
    test {
        testLong1
        testLong2
        testShort1
        testShort2
        testBigDecimal
        testBigInteger
    }
}
""")

        then:
            result.errors.isEmpty()
            result.data.test.testLong1 == Long.MAX_VALUE
            result.data.test.testLong2 == Long.MIN_VALUE
            result.data.test.testShort1 == Short.MAX_VALUE
            result.data.test.testShort2 == Short.MIN_VALUE
            result.data.test.testBigDecimal == BigDecimal.ZERO
            result.data.test.testBigInteger == BigInteger.ONE
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
        long testLong1 = Long.MAX_VALUE
        Long testLong2 = Long.MIN_VALUE
        short testShort1 = Short.MAX_VALUE
        Short testShort2 = Short.MIN_VALUE
        BigDecimal testBigDecimal = BigDecimal.ZERO
        BigInteger testBigInteger = BigInteger.ONE
    }

}
