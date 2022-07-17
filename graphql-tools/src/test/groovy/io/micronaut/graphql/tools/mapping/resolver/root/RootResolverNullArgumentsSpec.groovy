package io.micronaut.graphql.tools.mapping.resolver.root

import io.micronaut.context.annotation.Requires
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLInput
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import org.intellij.lang.annotations.Language

class RootResolverNullArgumentsSpec extends AbstractTest {

    static final String SPEC_NAME = "RootResolverNullArgumentsSpec"

    void "test TODO"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  hello(input: HelloInput, inputList: [HelloInput], inputString: String, inputStringList: [String]): String
}

input HelloInput {
  firstName: String
  lastName: String
}
"""

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""{
hello
}""")

        then:
            result.errors.isEmpty()
            result.data.hello == null

        when:
            result = executeQuery("""{
hello(input: null, inputList: null, inputString: null, inputStringList: null)
}""")

        then:
            result.errors.isEmpty()
            result.data.hello == null
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        String hello(HelloInput input, List<HelloInput> inputList, String inputString, List<String> inputStringList) {
            assert input == null
            assert inputList == null
            assert inputString == null
            assert inputStringList == null
            return null
        }
    }

    @GraphQLInput
    static class HelloInput {
        String firstName
        String lastName
    }

}
