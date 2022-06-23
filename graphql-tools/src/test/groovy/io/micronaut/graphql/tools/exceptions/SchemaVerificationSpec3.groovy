package io.micronaut.graphql.tools.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import org.intellij.lang.annotations.Language

class SchemaVerificationSpec3 extends AbstractTest {

    static final String SPEC_NAME = "SchemaVerificationSpec3"

    void "test mapping custom GraphQL type to a wrong class"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  currentUser: User
}

type User {
  username: String
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
            e.cause.message == """The field is mapped to the built-in class, when required custom Java class.
  GraphQL type: Query
  GraphQL field: currentUser
  Mapped class: ${SchemaVerificationSpec3.Query.name}
  Mapped method: currentUser()
  Provided class: ${Integer.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'currentUser'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec3.Query
            e.cause.mappingDetails.mappedMethod == 'currentUser()'
            e.cause.providedClass == Integer
    }

    void "test mapping custom GraphQL type to a wrong class [required field]"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  currentUser: User!
}

type User {
  username: String
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
            e.cause.message == """The field is mapped to the built-in class, when required custom Java class.
  GraphQL type: Query
  GraphQL field: currentUser
  Mapped class: ${SchemaVerificationSpec3.Query.name}
  Mapped method: currentUser()
  Provided class: ${Integer.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'currentUser'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec3.Query
            e.cause.mappingDetails.mappedMethod == 'currentUser()'
            e.cause.providedClass == Integer
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        Integer currentUser() {
            return 0
        }
    }

}
