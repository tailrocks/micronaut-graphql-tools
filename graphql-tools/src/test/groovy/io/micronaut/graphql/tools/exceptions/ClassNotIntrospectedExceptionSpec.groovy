package io.micronaut.graphql.tools.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import org.intellij.lang.annotations.Language

class ClassNotIntrospectedExceptionSpec extends AbstractTest {

    static final String SPEC_NAME = "ClassNotIntrospectedExceptionSpec"

    @Language("GraphQL")
    static final String SCHEMA = """
schema {
  query: Query
}

type Query {
  user: User
}

type User {
  username: String
}
"""

    void "test root resolver returns not introspected class"() {
        when:
            startContext(SCHEMA, SPEC_NAME)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof ClassNotIntrospectedException
            e.cause.message == """The class ${User.name} is not introspected. Ensure the class is annotated with ${GraphQLType.name}.
  GraphQL type: Query
  GraphQL field: user
  Mapped class: ${Query.name}
  Mapped method: user()"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'user'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == "user()"
    }


    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return null
        }
    }

    static class User {
        String username() {
            return null
        }
    }

}