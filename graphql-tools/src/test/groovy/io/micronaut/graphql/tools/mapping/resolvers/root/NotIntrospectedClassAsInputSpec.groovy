package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLInput
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.exceptions.ClassNotIntrospectedException
import org.intellij.lang.annotations.Language

class NotIntrospectedClassAsInputSpec extends AbstractTest {

    static final String SPEC_NAME = "NotIntrospectedClassAsInputSpec"

    void "test root resolver use not introspected class as input argument"() {
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

        when:
            startContext(schema, SPEC_NAME)
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof ClassNotIntrospectedException
            e.cause.message == """The class ${PriceInput.name} is not introspected. Ensure the class is annotated with ${GraphQLInput.name}.
  GraphQL type: Query
  GraphQL field: price
  GraphQL argument: input
  Mapped class: ${Query.name}
  Mapped method: price(${PriceInput.name} input)"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'price'
            e.cause.mappingDetails.graphQlArgument == 'input'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == "price(${PriceInput.name} input)"
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        Float price(PriceInput input) {
            return 10.00f
        }
    }

    static class PriceInput {
        String from
        String to
    }

}
