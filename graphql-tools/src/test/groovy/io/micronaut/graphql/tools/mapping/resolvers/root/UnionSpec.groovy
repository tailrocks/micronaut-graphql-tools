package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.exceptions.ImplementationNotFoundException
import org.intellij.lang.annotations.Language

class UnionSpec extends AbstractTest {

    static final String SPEC_NAME = "mapping.resolvers.root.TestSpec"

    @Language("GraphQL")
    static final String SCHEMA = """
schema {
  query: Query
}

type Query {
  unionTypeTest(securityError: Boolean!): PayloadError
}

union PayloadError = SecurityError | ValidationError

type SecurityError {
  code: String!
}

type ValidationError {
  code: Int!
}
"""

    void "test todo"() {
        when:
            startContext(SCHEMA, SPEC_NAME)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof ImplementationNotFoundException
            e.cause.message == """Can not find implementation class for the interface ${User.name}.
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
        PayloadError unionTypeTest(boolean securityError) {
            return null
        }
    }

    static interface PayloadError {
    }

    @GraphQLType
    static class SecurityError implements PayloadError {
        String code() {
            return "AUTH"
        }
    }

    @GraphQLType
    static class ValidationError implements PayloadError {
        Integer code() {
            return 123
        }
    }

}
