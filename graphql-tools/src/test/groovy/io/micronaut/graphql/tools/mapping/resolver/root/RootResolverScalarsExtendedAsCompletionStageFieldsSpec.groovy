package io.micronaut.graphql.tools.mapping.resolver.root

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import org.intellij.lang.annotations.Language

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class RootResolverScalarsExtendedAsCompletionStageFieldsSpec extends AbstractTest {

    static final String SPEC_NAME = "RootResolverScalarsExtendedAsCompletionStageFieldsSpec"

    void "mapping extended graphql-java scalars in root resolver"() {
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
  testLong: Long
  testShort: Short
  testBigDecimal: BigDecimal
  testBigInteger: BigInteger
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    testLong
    testShort
    testBigDecimal
    testBigInteger
}
""")

        then:
            result.errors.isEmpty()
            result.data.testLong == Long.MIN_VALUE
            result.data.testShort == Short.MIN_VALUE
            result.data.testBigDecimal == BigDecimal.ZERO
            result.data.testBigInteger == BigInteger.ONE
    }

    void "mapping extended graphql-java scalars in root resolver [required field]"() {
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
  testLong: Long!
  testShort: Short!
  testBigDecimal: BigDecimal!
  testBigInteger: BigInteger!
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    testLong
    testShort
    testBigDecimal
    testBigInteger
}
""")

        then:
            result.errors.isEmpty()
            result.data.testLong == Long.MIN_VALUE
            result.data.testShort == Short.MIN_VALUE
            result.data.testBigDecimal == BigDecimal.ZERO
            result.data.testBigInteger == BigInteger.ONE
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        CompletionStage<Long> testLong() {
            return CompletableFuture.completedFuture(Long.MIN_VALUE)
        }

        CompletionStage<Short> testShort() {
            return CompletableFuture.completedFuture(Short.MIN_VALUE)
        }

        CompletionStage<BigDecimal> testBigDecimal() {
            return CompletableFuture.completedFuture(BigDecimal.ZERO)
        }

        CompletionStage<BigInteger> testBigInteger() {
            return CompletableFuture.completedFuture(BigInteger.ONE)
        }
    }

}
