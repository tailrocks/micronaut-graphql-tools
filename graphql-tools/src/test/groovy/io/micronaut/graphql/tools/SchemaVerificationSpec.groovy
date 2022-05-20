package io.micronaut.graphql.tools

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.exceptions.CustomTypeMappedToBuiltInClassException
import io.micronaut.graphql.tools.exceptions.IncorrectArgumentCountException
import io.micronaut.graphql.tools.exceptions.IncorrectBuiltInScalarMappingException
import io.micronaut.graphql.tools.exceptions.MethodNotFoundException
import io.micronaut.graphql.tools.exceptions.SchemaDefinitionEmptyException
import org.intellij.lang.annotations.Language

class SchemaVerificationSpec1 extends AbstractTest {

    void "test schema definition"() {
        given:
            @Language("GraphQL")
            String schema = """
type Query {
  hello: String
}
"""

            startContext(schema, null)

        when:
            executeQuery("""
{ 
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof SchemaDefinitionEmptyException
            e.cause.message == """Schema definition is not set. Make sure your GraphQL schema contains such definition:
  schema {
    query: Query
    mutation: Mutation
  }"""
    }

    void "test Query method not found"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  hello: String
}
"""

            startContext(schema, null)

        when:
            executeQuery("""
{ 
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MethodNotFoundException
            e.cause.message == 'The method `hello` not found in any GraphQL query resolvers'
            e.cause.methodName == 'hello'
    }

    void "test Mutation method not found"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  mutation: Mutation
}

type Mutation {
  hello: String
}
"""

            startContext(schema, null)

        when:
            executeQuery("""
{ 
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MethodNotFoundException
            e.cause.message == 'The method `hello` not found in any GraphQL query resolvers'
            e.cause.methodName == 'hello'
    }

}

class SchemaVerificationSpec2 extends AbstractTest {

    static final String SPEC_NAME = "SchemaVerificationSpec2"

    void "test mapping built-in GraphQL type to a wrong class"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  hello: String
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
            e.cause instanceof IncorrectBuiltInScalarMappingException
            e.cause.message == """The field is mapped to the incorrect class.
  GraphQL type: Query
  GraphQL field: hello
  Mapped class: ${SchemaVerificationSpec2.name}\$${Query.simpleName}
  Mapped method: hello
  Provided class: ${Integer.name}
  Supported classes: ${String.name}"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'hello'
            e.cause.mappedClass == Query
            e.cause.mappedMethod == 'hello'
            e.cause.providedClass == Integer
            e.cause.supportedClasses == [String] as HashSet
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        Integer hello() {
            return 0
        }
    }

}

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
            def result = executeQuery("""
{
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof CustomTypeMappedToBuiltInClassException
            e.cause.message == """The field is mapped to the built-in class, but required custom Java class.
  GraphQL type: Query
  GraphQL field: currentUser
  Mapped class: ${SchemaVerificationSpec3.name}\$${Query.simpleName}
  Mapped method: currentUser
  Provided class: java.lang.Integer"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'currentUser'
            e.cause.mappedClass == Query
            e.cause.mappedMethod == 'currentUser'
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

class SchemaVerificationSpec4 extends AbstractTest {

    static final String SPEC_NAME_1 = "SchemaVerificationSpec4_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec4_2"

    @Language("GraphQL")
    static final String SCHEMA = """
schema {
  query: Query
}

type Query {
  username: String
}
"""

    void "test method in root resolver has one argument when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 1, required 0 arg(s).
  GraphQL type: Query
  GraphQL field: username
  Mapped class: ${SchemaVerificationSpec4.name}\$${Query1.simpleName}
  Mapped method: username(java.lang.String uid)"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == Query1
            e.cause.mappedMethod == 'username(java.lang.String uid)'
            e.cause.providedCount == 1
            e.cause.requiredCount == 0
    }

    void "test method in root resolver has one argument (exclude DataFetchingEnvironment) when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 1, required 0 arg(s).
  GraphQL type: Query
  GraphQL field: username
  Mapped class: ${SchemaVerificationSpec4.name}\$${Query2.simpleName}
  Mapped method: username(java.lang.String uid,graphql.schema.DataFetchingEnvironment dfe)"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == Query2
            e.cause.mappedMethod == 'username(java.lang.String uid,graphql.schema.DataFetchingEnvironment dfe)'
            e.cause.providedCount == 1
            e.cause.requiredCount == 0
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        String username(String uid) {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        String username(String uid, DataFetchingEnvironment dfe) {
            return null
        }
    }
}

class SchemaVerificationSpec5 extends AbstractTest {

    static final String SPEC_NAME_1 = "SchemaVerificationSpec5_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec5_2"

    @Language("GraphQL")
    static final String SCHEMA = """
schema {
  query: Query
}

type Query {
  username(uid: ID): String
}
"""

    void "test method in root resolver has zero arguments when GraphQL schema has one"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 0, required 1 arg(s): (ID uid)
  GraphQL type: Query
  GraphQL field: username
  Mapped class: ${SchemaVerificationSpec5.name}\$${Query1.simpleName}
  Mapped method: username()"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == Query1
            e.cause.mappedMethod == 'username()'
            e.cause.providedCount == 0
            e.cause.requiredCount == 1
    }

    void "test method in root resolver has zero arguments (exclude DataFetchingEnvironment) when GraphQL schema has one"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 0, required 1 arg(s): (ID uid)
  GraphQL type: Query
  GraphQL field: username
  Mapped class: ${SchemaVerificationSpec5.name}\$${Query2.simpleName}
  Mapped method: username(graphql.schema.DataFetchingEnvironment dfe)"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == Query2
            e.cause.mappedMethod == 'username(graphql.schema.DataFetchingEnvironment dfe)'
            e.cause.providedCount == 0
            e.cause.requiredCount == 1
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        String username() {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        String username(DataFetchingEnvironment dfe) {
            return null
        }
    }

}

class SchemaVerificationSpec6 extends AbstractTest {

    static final String SPEC_NAME = "SchemaVerificationSpec6"

    void "test union"() {
        given:
            @Language("GraphQL")
            String schema = """
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

            startContext(schema, SPEC_NAME)

        when:
            def result = executeQuery("""
{
    unionTypeTest
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof CustomTypeMappedToBuiltInClassException
            e.cause.message == """The field is mapped to built-in class, but required custom Java class
  GraphQL type: User
  GraphQL field: currentUser
  Mapped class: ${SchemaVerificationSpec6.name}\$${Query5.simpleName}
  Mapped method name: currentUser
  Provided class: java.lang.Integer"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'currentUser'
            e.cause.mappedClass == Query2
            e.cause.mappedMethod == 'currentUser'
            e.cause.providedClass == Integer
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query5 {
        Integer currentUser() {
            return 0
        }
    }

}
