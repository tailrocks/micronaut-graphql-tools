package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.exceptions.MethodNotFoundException
import org.intellij.lang.annotations.Language

class MethodNotFoundSpec extends AbstractTest {

    static final String SPEC_NAME = "mapping.resolvers.root.MethodNotFoundSpec"

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

            startContext(schema, SPEC_NAME)

        when:
            getGraphQLBean()

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MethodNotFoundException
            e.cause.message == "The method `hello` not found in any root resolvers: [${Query1.name}, ${Query2.name}]."
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

            startContext(schema, SPEC_NAME)

        when:
            getGraphQLBean()

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MethodNotFoundException
            e.cause.message == "The method `hello` not found in any root resolvers: [${Query1.name}, ${Query2.name}]."
            e.cause.methodName == 'hello'
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query1 {
        String test1() {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query2 {
        String test2() {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Mutation {
        // ignored as no methods was found inside this class
    }

}
