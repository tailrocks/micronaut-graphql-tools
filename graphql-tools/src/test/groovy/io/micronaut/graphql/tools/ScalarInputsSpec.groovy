package io.micronaut.graphql.tools

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.annotation.GraphQLParameterized
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import org.intellij.lang.annotations.Language

class ScalarInputsSpec1 extends AbstractTest {

    static final String SPEC_NAME = "ScalarInputsSpec1"

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


class ScalarInputsSpec2 extends AbstractTest {

    static final String SPEC_NAME = "ScalarInputsSpec2"

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


class ScalarInputsSpec3 extends AbstractTest {

    static final String SPEC_NAME = "ScalarInputsSpec3"

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


class ScalarInputsSpec4 extends AbstractTest {

    static final String SPEC_NAME = "ScalarInputsSpec4"

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


class ScalarInputsSpec5 extends AbstractTest {

    static final String SPEC_NAME = "ScalarInputsSpec5"

    void "test mapping standard graphql scalars as inputs in type resolver"() {
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

    void "test mapping standard graphql scalars as inputs in type resolver [required field]"() {
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
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(Test.class)
    static class TestResolver {
        String hello(
                Test test,
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


class ScalarInputsSpec6 extends AbstractTest {

    static final String SPEC_NAME = "ScalarInputsSpec6"

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
