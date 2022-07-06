package io.micronaut.graphql.tools.mapping.resolvers.type

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import io.micronaut.graphql.tools.exceptions.InvalidSourceArgumentException
import org.intellij.lang.annotations.Language

class SourceArgumentSpec extends AbstractTest {

    static final String SPEC_NAME = "mapping.resolvers.type.SourceArgumentSpec"

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
  avatar: String
}
"""

    void "test source object passed to GraphQLTypeResolver's method"() {
        given:
            startContext(SCHEMA, SPEC_NAME)

        when:
            def result = executeQuery("""
{ 
    user {
        username
        avatar
    }
}
""")

        then:
            result.errors.isEmpty()
            result.dataPresent
            result.data.user.username == 'test'
            result.data.user.avatar == 'pig.png'
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return new User(username: 'test')
        }
    }

    @GraphQLType
    static class User {
        String username
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(User.class)
    static class UserResolver {
        String avatar(User user) {
            assert user != null
            assert user.username == 'test'
            return 'pig.png'
        }
    }

}
