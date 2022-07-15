package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.exceptions.IncorrectClassMappingException
import org.intellij.lang.annotations.Language

class RootResolverIncorrectFieldMappingXXXXXXSpec extends AbstractTest {

    static final String SPEC_NAME = "RootResolverIncorrectFieldMappingXXXXXXSpec"

    void "test enum annotated with GraphQLType instead of custom class"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  testString: [String]
}
"""

            startContext(schema, SPEC_NAME)

        when:
            getGraphQLBean()

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to an enum, when required a custom class.
  GraphQL type: User
  GraphQL field: username
  Provided class: ${User.name}"""
            e.cause.mappingContext.graphQlType == 'User'
            e.cause.mappingContext.graphQlField == 'username'
            e.cause.providedClass == User
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        String testString() {
            return null
        }
    }

}
