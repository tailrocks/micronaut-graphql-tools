package io.micronaut.graphql.tools.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import org.intellij.lang.annotations.Language

class MissingEnumValuesExceptionSpec extends AbstractTest {

    static final String SPEC_NAME = "MissingEnumValuesExceptionSpec"

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

            startContext(schema, SPEC_NAME)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MissingEnumValuesException
            e.cause.message == """Some enum values are missing.
  GraphQL type: Query
  GraphQL field: month
  Mapped class: ${Query.name}
  Mapped method: month()
  Missing values: MARCH"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'month'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == 'month()'
            e.cause.missingValues == ['MARCH']
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        Month month() {
            return null
        }
    }

    static enum Month {
        JANUARY,
        FEBRUARY
    }

}
