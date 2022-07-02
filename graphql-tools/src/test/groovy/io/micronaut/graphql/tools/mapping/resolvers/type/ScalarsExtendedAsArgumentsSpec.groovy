package io.micronaut.graphql.tools.mapping.resolvers.type

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import org.intellij.lang.annotations.Language

class ScalarsExtendedAsArgumentsSpec extends AbstractTest {

    static final String SPEC_NAME = "mapping.resolvers.type.ScalarsExtendedAsInputsSpec"

    void "test mapping extended graphql-java scalars as inputs in type resolver"() {
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
  hello(
      testLong1: Long
      testLong2: Long
      testShort1: Short
      testShort2: Short
      testBigDecimal: BigDecimal
      testBigInteger: BigInteger
  ): String
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    test {
        hello(
            testLong1: 123,
            testLong2: -123,
            testShort1: 1,
            testShort2: -1,
            testBigDecimal: 10.00,
            testBigInteger: 10
        )
    }
}
""")

        then:
            result.errors.isEmpty()
            result.data.test.hello == 'World'
    }

    void "test mapping extended graphql-java scalars as inputs in type resolver [required field]"() {
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
  hello(
      testLong1: Long!
      testLong2: Long!
      testShort1: Short!
      testShort2: Short!
      testBigDecimal: BigDecimal!
      testBigInteger: BigInteger
  ): String
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    test {
        hello(
            testLong1: 123,
            testLong2: -123,
            testShort1: 1,
            testShort2: -1,
            testBigDecimal: 10.00,
            testBigInteger: 10
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
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(Test.class)
    static class TestResolver {
        String hello(
                Test test,
                long testLong1,
                Long testLong2,
                short testShort1,
                Short testShort2,
                BigDecimal testBigDecimal,
                BigInteger testBigInteger
        ) {
            assert testLong1 == 123
            assert testLong2 == -123
            assert testShort1 == 1
            assert testShort2 == -1
            assert testBigDecimal == BigDecimal.valueOf(10.00)
            assert testBigInteger == BigInteger.valueOf(10)

            return "World"
        }
    }

}
