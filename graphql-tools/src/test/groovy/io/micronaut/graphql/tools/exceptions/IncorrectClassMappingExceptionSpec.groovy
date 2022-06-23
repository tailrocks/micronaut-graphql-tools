package io.micronaut.graphql.tools.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import org.intellij.lang.annotations.Language

class IncorrectClassMappingExceptionSpec extends AbstractTest {

    static final String SPEC_NAME = "SchemaVerificationSpec2"

    void "test mapping built-in GraphQL type to a wrong class"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  hello: String
}
"""

            startContext(schema, SPEC_NAME)

        when:
            executeQuery("""
{
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the incorrect class.
  GraphQL type: Query
  GraphQL field: hello
  Mapped class: ${IncorrectClassMappingExceptionSpec.Query.name}
  Mapped method: hello()
  Provided class: ${Integer.name}
  Supported classes: ${String.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'hello'
            e.cause.mappingDetails.mappedClass == IncorrectClassMappingExceptionSpec.Query
            e.cause.mappingDetails.mappedMethod == 'hello()'
            e.cause.providedClass == Integer
            e.cause.supportedClasses == [String] as HashSet
    }

    void "test mapping built-in GraphQL type to a wrong class [required field]"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  hello: String!
}
"""

            startContext(schema, SPEC_NAME)

        when:
            executeQuery("""
{
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the incorrect class.
  GraphQL type: Query
  GraphQL field: hello
  Mapped class: ${IncorrectClassMappingExceptionSpec.Query.name}
  Mapped method: hello()
  Provided class: ${Integer.name}
  Supported classes: ${String.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'hello'
            e.cause.mappingDetails.mappedClass == IncorrectClassMappingExceptionSpec.Query
            e.cause.mappingDetails.mappedMethod == 'hello()'
            e.cause.providedClass == Integer
            e.cause.supportedClasses == [String] as HashSet
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        Integer hello() {
            return 0
        }
    }

}
