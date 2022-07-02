package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLInput
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.exceptions.IncorrectClassMappingException
import org.intellij.lang.annotations.Language

class InterfaceAsInputSpec extends AbstractTest {

    static final String SPEC_NAME = "ClassNotIntrospectedExceptionSpec3"

    void "test root resolver use interface as input argument"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  price(input: PriceInput): Float
}

input PriceInput {
  from: String!
  to: String!
}
"""

            startContext(schema, SPEC_NAME)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The argument is mapped to interface, when required custom Java class.
  GraphQL type: Query
  GraphQL field: price
  GraphQL argument: input
  Mapped class: ${Query.name}
  Mapped method: price(${PriceInput.name} input)
  Provided class: ${PriceInput.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'price'
            e.cause.mappingDetails.graphQlArgument == 'input'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == "price(${PriceInput.name} input)"
            e.cause.providedClass == PriceInput
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        Float price(PriceInput input) {
            return 10.00f
        }
    }

    @GraphQLInput
    static interface PriceInput {
        String from
        String to
    }

}