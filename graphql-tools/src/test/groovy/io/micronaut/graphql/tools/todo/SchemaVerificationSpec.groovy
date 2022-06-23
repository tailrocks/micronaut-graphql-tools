package io.micronaut.graphql.tools.todo

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.exceptions.IncorrectClassMappingException
import io.micronaut.graphql.tools.exceptions.MappingConflictException
import io.micronaut.graphql.tools.exceptions.MissingEnumValuesException
import org.intellij.lang.annotations.Language

// TODO
class SchemaVerificationSpec extends AbstractTest {

    static final String SPEC_NAME_1 = "SchemaVerificationSpec12_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec12_2"
    static final String SPEC_NAME_3 = "SchemaVerificationSpec12_3"
    static final String SPEC_NAME_4 = "SchemaVerificationSpec12_4"

    void "test GraphQL schema enum mapped to a Java class"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  month: Month
}

enum Month {
  JANUARY
  FEBRUARY
  MARCH
}
"""

            startContext(schema, SPEC_NAME_1)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the class, when required Enum.
  GraphQL type: Query
  GraphQL field: month
  Mapped class: ${Query1.name}
  Mapped method: month()
  Provided class: ${MyMonth.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'month'
            e.cause.mappingDetails.mappedClass == Query1
            e.cause.mappingDetails.mappedMethod == 'month()'
            e.cause.providedClass == MyMonth
    }

    void "test GraphQL schema enum as a input parameter mapped to a Java class"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  displayName(value: Month!): String
}

enum Month {
  JANUARY
  FEBRUARY
  MARCH
}
"""

            startContext(schema, SPEC_NAME_2)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the class, when required Enum.
  GraphQL type: Query
  GraphQL field: displayName
  Mapped class: ${SchemaVerificationSpec12.name}\$${Query2.simpleName}
  Mapped method: displayName(java.lang.String value)
  Provided class: java.lang.String"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'displayName'
            e.cause.mappingDetails.mappedClass == Query2
            e.cause.mappingDetails.mappedMethod == 'displayName(java.lang.String value)'
    }

    void "test mapping to an enum with missed values"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  month: Month
}

enum Month {
  JANUARY
  FEBRUARY
  MARCH
}
"""

            startContext(schema, SPEC_NAME_3)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MissingEnumValuesException
            e.cause.message == """Some enum values are missing.
  GraphQL type: Query
  GraphQL field: month
  Mapped class: ${Query3.name}
  Mapped method: month()
  Missing values: MARCH"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'month'
            e.cause.mappingDetails.mappedClass == Query3
            e.cause.mappingDetails.mappedMethod == 'month()'
            e.cause.missingValues == ['MARCH']
    }

    void "test enum mapped to a different classes"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  currentMonth: Month
  nextMonth: Month
}

enum Month {
  JANUARY
  FEBRUARY
  MARCH
}
"""

            startContext(schema, SPEC_NAME_4)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MappingConflictException
            e.cause.message == """Unable to map GraphQL enum `Month` to ${AnotherMonth4.name}, as it is already mapped to ${Month4.name}.
  GraphQL type: Query
  GraphQL field: nextMonth
  Mapped class: ${Query4.name}
  Mapped method: nextMonth()"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'nextMonth'
            e.cause.mappingDetails.mappedClass == Query4
            e.cause.mappingDetails.mappedMethod == 'nextMonth()'
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        MyMonth month() {
            return null
        }
    }

    @GraphQLType
    static class MyMonth {
        String getJanuary() {
            return "JAN"
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        String displayName(String value) {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_3)
    @GraphQLRootResolver
    static class Query3 {
        Month3 month() {
            return null
        }
    }

    static enum Month3 {
        JANUARY,
        FEBRUARY
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_4)
    @GraphQLRootResolver
    static class Query4 {
        Month4 currentMonth() {
            return null
        }

        AnotherMonth4 nextMonth() {
            return null
        }
    }

    static enum Month4 {
        JANUARY,
        FEBRUARY,
        MARCH
    }

    static enum AnotherMonth4 {
        JANUARY,
        FEBRUARY,
        MARCH
    }

}

// TODO validate input mapping
// TODO validate type field mapping
// TODO union mapping
