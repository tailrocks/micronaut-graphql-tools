package io.micronaut.graphql.tools

import io.micronaut.context.exceptions.BeanInstantiationException
import org.intellij.lang.annotations.Language

class SchemaVerificationSpec extends AbstractTest {

    void "test Query method not found"() {
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

            startContext(schema)

        when:
            executeQuery("""
{ 
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MethodNotFoundException
            e.cause.message == 'The method `hello` not found in any GraphQL query resolvers'
            e.cause.methodName == 'hello'
    }

    void "test Mutation method not found"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  mutation: Mutation
}

type Mutation {
  hello: String
}
"""

            startContext(schema)

        when:
            executeQuery("""
{ 
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MethodNotFoundException
            e.cause.message == 'The method `hello` not found in any GraphQL query resolvers'
            e.cause.methodName == 'hello'
    }

}
