package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.exceptions.IncorrectClassMappingException
import org.intellij.lang.annotations.Language

class RootResolverIncorrectFieldMappingEnumToCustomClassSpec extends AbstractTest {

    static final String SPEC_NAME = "RootResolverIncorrectFieldMappingEnumToCustomClassSpec"

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
            getGraphQLBean()

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to a custom class, when required an enum.
  GraphQL type: Query
  GraphQL field: month
  Mapped class: ${Query.name}
  Mapped method: month()
  Provided class: ${MyMonth.name}"""
            e.cause.mappingContext.graphQlType == 'Query'
            e.cause.mappingContext.graphQlField == 'month'
            e.cause.mappingContext.mappedClass == Query
            e.cause.mappingContext.mappedMethod == 'month()'
            e.cause.providedClass == MyMonth
    }


    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
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

}
