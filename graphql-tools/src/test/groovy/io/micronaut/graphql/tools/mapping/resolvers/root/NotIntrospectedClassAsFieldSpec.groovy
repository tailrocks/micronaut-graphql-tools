package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.exceptions.ClassNotIntrospectedException
import org.intellij.lang.annotations.Language

class NotIntrospectedClassAsFieldSpec extends AbstractTest {

    static final String SPEC_NAME = "mapping.resolvers.root.NotIntrospectedClassAsFieldSpec"

    void "test root resolver returns not introspected class"() {
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
  username: String
}
"""
            startContext(schema, SPEC_NAME)

        when:
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof ClassNotIntrospectedException
            e.cause.message == """The class ${User.name} is not introspected. Ensure the class is annotated with ${GraphQLType.name}.
  GraphQL type: Query
  GraphQL field: user
  Mapped class: ${Query.name}
  Mapped method: user()"""
            e.cause.mappingContext.graphQlType == 'Query'
            e.cause.mappingContext.graphQlField == 'user'
            e.cause.mappingContext.mappedClass == Query
            e.cause.mappingContext.mappedMethod == "user()"
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
