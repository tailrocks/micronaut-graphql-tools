package io.micronaut.graphql.tools.todo

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import io.micronaut.graphql.tools.exceptions.IncorrectArgumentCountExceptionSpec3
import io.micronaut.graphql.tools.exceptions.IncorrectClassMappingException
import org.intellij.lang.annotations.Language

class SchemaVerificationSpec11 extends AbstractTest {

    static final String SPEC_NAME = "SchemaVerificationSpec11"

    void "test union"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  user: User
}

type User {
  username: String!
  gravatar: String
}
"""

            startContext(schema, SPEC_NAME)

        when:
            executeQuery("""
{
    user {
        username
        gravatar
    }
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to built-in class, but required custom Java class
  GraphQL type: User
  GraphQL field: currentUser
  Mapped class: ${IncorrectArgumentCountExceptionSpec3.name}\$${Query5.simpleName}
  Mapped method name: currentUser
  Provided class: ${Integer.name}"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'currentUser'
            e.cause.mappedClass == Query2
            e.cause.mappedMethod == 'currentUser'
            e.cause.providedClass == Integer
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query1 {
        SchemaVerificationSpec11.User1 user() {
            return null
        }
    }

    @GraphQLType
    static class User1 {
        String getUsername() {
            return "abc"
        }
    }

    // TODO restore me
    //@GraphQLTypeResolver
    @GraphQLTypeResolver(SchemaVerificationSpec11.User1)
    static class User1Resolver {
        String gravatar(SchemaVerificationSpec11.User1 user) {
            return null
        }
    }

}
