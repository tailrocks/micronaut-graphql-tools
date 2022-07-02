package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.exceptions.IncorrectClassMappingException
import org.intellij.lang.annotations.Language

class IncorrectClassMappingExceptionSpec5 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectClassMappingExceptionSpec5"

    void "test GraphQL schema enum as a input parameter mapped to a Java class"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  displayName(value: Month): String
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
  GraphQL field: displayName
  GraphQL argument: value
  Mapped class: ${Query.name}
  Mapped method: displayName(java.lang.String value)
  Provided class: java.lang.String"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'displayName'
            e.cause.mappingDetails.graphQlArgument == 'value'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == 'displayName(java.lang.String value)'
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        String displayName(String value) {
            return null
        }
    }

}
