package io.micronaut.graphql.tools.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLInput
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import org.intellij.lang.annotations.Language

class ClassNotIntrospectedExceptionSpec1 extends AbstractTest {

    static final String SPEC_NAME = "ClassNotIntrospectedExceptionSpec1"

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


class ClassNotIntrospectedExceptionSpec2 extends AbstractTest {

    static final String SPEC_NAME = "ClassNotIntrospectedExceptionSpec2"

    void "test root resolver use not introspected class as input argument"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  price(input: PriceInput!): Float
}

input PriceInput {
  from: String!
  to: String!
}
"""

            startContext(schema, SPEC_NAME)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof ClassNotIntrospectedException
            e.cause.message == """The class ${PriceInput.name} is not introspected. Ensure the class is annotated with ${GraphQLInput.name}.
  GraphQL type: Query
  GraphQL field: price
  Mapped class: ${Query.name}
  Mapped method: price(${PriceInput.name} input)"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'price'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == "price(${PriceInput.name} input)"
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        Float price(PriceInput input) {
            return 10.00f
        }
    }

    static class PriceInput {
        String from
        String to
    }

}


class ClassNotIntrospectedExceptionSpec3 extends AbstractTest {

    static final String SPEC_NAME = "ClassNotIntrospectedExceptionSpec3"

    void "test root resolver use not introspected class as input argument"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  price(input: PriceInput!): Float
}

input PriceInput {
  from: String!
  to: String!
}
"""

            startContext(schema, SPEC_NAME)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof ClassNotIntrospectedException
            e.cause.message == """The class ${PriceInput.name} is not introspected. Ensure the class is annotated with ${GraphQLInput.name}.
  GraphQL type: Query
  GraphQL field: price
  Mapped class: ${Query.name}
  Mapped method: price(${PriceInput.name} input)"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'price'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == "price(${PriceInput.name} input)"
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        Float price(PriceInput input) {
            return 10.00f
        }
    }

    @GraphQLInput
    static interface PriceInput {
        String from
        String to
    }

}


class ClassNotIntrospectedExceptionSpec4 extends AbstractTest {

    static final String SPEC_NAME = "ClassNotIntrospectedExceptionSpec4"

    void "test root resolver use not introspected class as input argument"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  price(input: PriceInput!): Float
}

input PriceInput {
  from: String!
  to: String!
}
"""

            startContext(schema, SPEC_NAME)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof ClassNotIntrospectedException
            e.cause.message == """The class ${PriceInput.name} is not introspected. Ensure the class is annotated with ${GraphQLInput.name}.
  GraphQL type: Query
  GraphQL field: price
  Mapped class: ${Query.name}
  Mapped method: price(${PriceInput.name} input)"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'price'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == "price(${PriceInput.name} input)"
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        Float price(PriceInput input) {
            return 10.00f
        }
    }

    @GraphQLInput
    static enum PriceInput {
        TEST
    }

}
