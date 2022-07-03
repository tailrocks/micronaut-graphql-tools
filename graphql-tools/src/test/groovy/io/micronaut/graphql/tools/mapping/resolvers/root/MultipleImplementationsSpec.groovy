package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import io.micronaut.graphql.tools.exceptions.MultipleImplementationsFoundException
import org.intellij.lang.annotations.Language

class MultipleImplementationsSpec extends AbstractTest {

    static final String SPEC_NAME = "mapping.resolvers.root.MultipleImplementationsFoundExceptionSpec"

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

    void "test root resolver returns interface which has multiple introspected implementation classes"() {
        given:
            startContext(SCHEMA, SPEC_NAME)

        when:
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MultipleImplementationsFoundException
            e.cause.message == """Found multiple implementations for the interface ${User.name}.
  GraphQL type: Query
  GraphQL field: user
  Mapped class: ${Query.name}
  Mapped method: user()
  Implementation classes: ${UserAltImpl.name}, ${UserImpl.name}"""
            e.cause.mappingContext.graphQlType == 'Query'
            e.cause.mappingContext.graphQlField == 'user'
            e.cause.mappingContext.mappedClass == Query
            e.cause.mappingContext.mappedMethod == "user()"
            e.cause.implementationClasses == [UserAltImpl, UserImpl]
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    static interface User {
    }

    @GraphQLType(User)
    static class UserImpl implements User {
    }

    @GraphQLType(User)
    static class UserAltImpl implements User {
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(User.class)
    static class UserResolver {
        String username(User user) {
            return null
        }
    }

}
