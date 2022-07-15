package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.exceptions.IncorrectClassMappingException
import org.intellij.lang.annotations.Language

class RootResolverIncorrectArgumentMappingXXXXXXXSpec extends AbstractTest {

    static final String SPEC_NAME = "RootResolverIncorrectArgumentMappingXXXXXXXSpec"

    void "test input argument use enum instead of introspected custom class"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  testString(input: [String]): String
}
"""

            startContext(schema, SPEC_NAME)

        when:
            getGraphQLBean()

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The argument is mapped to an enum, when required a custom class.
  GraphQL type: Query
  GraphQL field: price
  GraphQL argument: input
  Mapped class: ${Query.name}
  Mapped method: price(${PriceInput.name} input)
  Provided class: ${PriceInput.name}"""
            e.cause.mappingContext.graphQlType == 'Query'
            e.cause.mappingContext.graphQlField == 'price'
            e.cause.mappingContext.graphQlArgument == 'input'
            e.cause.mappingContext.mappedClass == Query
            e.cause.mappingContext.mappedMethod == "price(${PriceInput.name} input)"
            e.cause.providedClass == PriceInput
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        String testString(String input) {
            return null
        }
    }

}
