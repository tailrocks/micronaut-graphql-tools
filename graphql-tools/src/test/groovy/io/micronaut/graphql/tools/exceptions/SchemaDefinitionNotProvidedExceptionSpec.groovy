package io.micronaut.graphql.tools.exceptions

import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import org.intellij.lang.annotations.Language

class SchemaDefinitionNotProvidedExceptionSpec extends AbstractTest {

    void "test schema definition"() {
        given:
            @Language("GraphQL")
            String schema = """
type Query {
  hello: String
}
"""

            startContext(schema, null)

        when:
            executeQuery("""
{ 
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof SchemaDefinitionNotProvidedException
            e.cause.message == """Schema definition is not set. Make sure your GraphQL schema contains such definition:
  schema {
    query: Query
    mutation: Mutation
  }"""
    }

}
