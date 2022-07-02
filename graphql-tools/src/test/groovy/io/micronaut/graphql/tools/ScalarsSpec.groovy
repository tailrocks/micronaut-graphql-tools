package io.micronaut.graphql.tools

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.annotation.GraphQLParameterized
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import org.intellij.lang.annotations.Language

class ScalarsSpec1 extends AbstractTest {

    static final String SPEC_NAME = "ScalarsSpec1"

    void "test mapping standard graphql scalars"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  testString: String
  testBoolean1: Boolean
  testBoolean2: Boolean
  testInt1: Int
  testInt2: Int
  testFloat1: Float
  testFloat2: Float
  testID: ID
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    testString
    testBoolean1
    testBoolean2
    testInt1
    testInt2
    testFloat1
    testFloat2
    testID
}
""")

        then:
            result.errors.isEmpty()
            result.data.testString == 'test'
            result.data.testBoolean1 == true
            result.data.testBoolean2 == false
            result.data.testInt1 == Integer.MAX_VALUE
            result.data.testInt2 == Integer.MIN_VALUE
            result.data.testFloat1 == Float.MAX_VALUE as Double
            result.data.testFloat2 == Float.MIN_VALUE as Double
            result.data.testID == 'id'
    }

    void "test mapping standard graphql scalars [required field]"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  testString: String!
  testBoolean1: Boolean!
  testBoolean2: Boolean!
  testInt1: Int!
  testInt2: Int!
  testFloat1: Float!
  testFloat2: Float!
  testID: ID!
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    testString
    testBoolean1
    testBoolean2
    testInt1
    testInt2
    testFloat1
    testFloat2
    testID
}
""")

        then:
            result.errors.isEmpty()
            result.data.testString == 'test'
            result.data.testBoolean1 == true
            result.data.testBoolean2 == false
            result.data.testInt1 == Integer.MAX_VALUE
            result.data.testInt2 == Integer.MIN_VALUE
            result.data.testFloat1 == Float.MAX_VALUE as Double
            result.data.testFloat2 == Float.MIN_VALUE as Double
            result.data.testID == 'id'
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        String testString() {
            return "test"
        }

        boolean testBoolean1() {
            return true
        }

        Boolean testBoolean2() {
            return Boolean.FALSE
        }

        int testInt1() {
            return Integer.MAX_VALUE
        }

        Integer testInt2() {
            return Integer.valueOf(Integer.MIN_VALUE)
        }

        float testFloat1() {
            return Float.MAX_VALUE
        }

        Float testFloat2() {
            return Float.valueOf(Float.MIN_VALUE)
        }

        String testID() {
            return "id"
        }
    }

}


class ScalarsSpec2 extends AbstractTest {

    static final String SPEC_NAME = "ScalarsSpec2"

    void "test mapping extended graphql-java scalars"() {
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

    void "test mapping extended graphql-java scalars [required field]"() {
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


class ScalarsSpec3 extends AbstractTest {

    static final String SPEC_NAME = "ScalarsSpec3"

    void "test mapping standard graphql scalars as inputs in root resolver"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
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
""")

        then:
            result.errors.isEmpty()
            result.data.hello == 'World'
    }

    void "test mapping standard graphql scalars as inputs in root resolver [required field]"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
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
""")

        then:
            result.errors.isEmpty()
            result.data.hello == 'World'
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
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


class ScalarsSpec4 extends AbstractTest {

    static final String SPEC_NAME = "ScalarsSpec2"

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
    hello(
        testLong1: 123,
        testLong2: -123,
        testShort1: 1,
        testShort2: -1,
        testBigDecimal: 10.00,
        testBigInteger: 10
    )
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
    hello(
        testLong1: 123,
        testLong2: -123,
        testShort1: 1,
        testShort2: -1,
        testBigDecimal: 10.00,
        testBigInteger: 10
    )
}
""")

        then:
            result.errors.isEmpty()
            result.data.hello == 'World'
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        String hello(
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


class ScalarsSpec5 extends AbstractTest {

    static final String SPEC_NAME = "ScalarsSpec5"

    void "test mapping standard graphql scalars as inputs in GraphQLType annotated entity"() {
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

    void "test mapping standard graphql scalars as inputs in GraphQLType annotated entity [required field]"() {
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
        @GraphQLParameterized
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


class ScalarsSpec6 extends AbstractTest {

    static final String SPEC_NAME = "ScalarsSpec6"

    void "test mapping extended graphql-java scalars as inputs in GraphQLType annotated entity"() {
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

    void "test mapping extended graphql-java scalars as inputs in GraphQLType annotated entity [required field]"() {
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
        @GraphQLParameterized
        String hello(
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
