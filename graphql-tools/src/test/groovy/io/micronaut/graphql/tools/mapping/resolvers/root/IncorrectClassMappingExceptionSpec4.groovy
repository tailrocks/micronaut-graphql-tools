package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.exceptions.IncorrectClassMappingException
import org.intellij.lang.annotations.Language

class IncorrectClassMappingExceptionSpec4 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectClassMappingExceptionSpec4"

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

            startContext(schema, SPEC_NAME)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the class, when required Enum.
  GraphQL type: Query
  GraphQL field: month
  Mapped class: ${IncorrectClassMappingExceptionSpec4.Query.name}
  Mapped method: month()
  Provided class: ${IncorrectClassMappingExceptionSpec4.MyMonth.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'month'
            e.cause.mappingDetails.mappedClass == IncorrectClassMappingExceptionSpec4.Query
            e.cause.mappingDetails.mappedMethod == 'month()'
            e.cause.providedClass == IncorrectClassMappingExceptionSpec4.MyMonth
    }


    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        IncorrectClassMappingExceptionSpec4.MyMonth month() {
            return null
        }
    }

    @GraphQLType
    static class MyMonth {
        String getJanuary() {
            return "JAN"
        }
    }

}
