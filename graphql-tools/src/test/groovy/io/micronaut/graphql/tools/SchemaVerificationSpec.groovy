package io.micronaut.graphql.tools

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.exceptions.CustomTypeMappedToBuiltInClassException
import io.micronaut.graphql.tools.exceptions.IncorrectBuiltInScalarMappingException
import io.micronaut.graphql.tools.exceptions.MethodNotFoundException
import io.micronaut.graphql.tools.exceptions.SchemaDefinitionEmptyException
import org.intellij.lang.annotations.Language

class SchemaVerificationSpec extends AbstractTest {

    static final String SCHEMA_CONFIG_1 = "SchemaVerificationSpec#1"
    static final String SCHEMA_CONFIG_2 = "SchemaVerificationSpec#2"

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
            e.cause.message == 'Schema definition is not set. Make sure your GraphQL schema contains such ' +
                    'definition:\n' +
                    'schema {\n' +
                    '  query: Query\n' +
                    '  mutation: Mutation\n' +
                    '}'
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

    void "test mapping built-in GraphQL type to wrong class"() {
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

            startContext(schema, SCHEMA_CONFIG_1)

        when:
            def result = executeQuery("""
{
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectBuiltInScalarMappingException
            e.cause.message == """The type `String` is mapped to incorrect class java.lang.Integer, supported classes: java.lang.String
  GraphQL type: String
  GraphQL field: hello
  Mapped class: ${SchemaVerificationSpec.name}\$${Query1.simpleName}
  Mapped method name: hello
  Provided class: java.lang.Integer
  Supported classes: java.lang.String"""
            e.cause.graphQlTypeName == 'String'
            e.cause.graphQlFieldName == 'hello'
            e.cause.mappedClass == Query1
            e.cause.mappedMethodName == 'hello'
            e.cause.providedClass == Integer
            e.cause.supportedClasses == [String] as HashSet
    }

    @Requires(property = 'spec.name', value = SCHEMA_CONFIG_1)
    @GraphQLRootResolver
    static class Query1 {
        Integer hello() {
            return 0
        }
    }

    void "test mapping custom GraphQL type to wrong class"() {
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

            startContext(schema, SCHEMA_CONFIG_2)

        when:
            def result = executeQuery("""
{
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof CustomTypeMappedToBuiltInClassException
            e.cause.message == """The field `currentUser` is mapped to built-in class class java.lang.Integer, but required custom Java class
  GraphQL type: User
  GraphQL field: currentUser
  Mapped class: ${SchemaVerificationSpec.name}\$${Query3.simpleName}
  Mapped method name: currentUser
  Provided class: java.lang.Integer"""
            e.cause.graphQlTypeName == 'User'
            e.cause.graphQlFieldName == 'currentUser'
            e.cause.mappedClass == Query3
            e.cause.mappedMethodName == 'currentUser'
            e.cause.providedClass == Integer
    }

    @Requires(property = 'spec.name', value = SCHEMA_CONFIG_2)
    @GraphQLRootResolver
    static class Query3 {
        Integer currentUser() {
            return 0
        }
    }

}
