package io.micronaut.graphql.tools


import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.exceptions.IncorrectBuiltInScalarMappingException
import io.micronaut.graphql.tools.exceptions.IncorrectMappingException
import io.micronaut.graphql.tools.exceptions.MethodNotFoundException
import io.micronaut.graphql.tools.exceptions.SchemaDefinitionEmptyException
import org.intellij.lang.annotations.Language

class SchemaVerificationSpec extends AbstractTest {

    void "test schema definition"() {
        given:
            @Language("GraphQL")
            String schema = """
type Query {
  hello: String
}
"""

            startContext(schema)

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

            startContext(schema)

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

            startContext(schema)

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

            startContext(schema, ['spec.name': 'SchemaVerificationSpec#1'])

        when:
            def result = executeQuery("""
{
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectBuiltInScalarMappingException
            e.cause.message == 'Type String is mapped to incorrect class class java.lang.Integer, supported classes: java.lang.String'
            e.cause.graphQlTypeName == 'String'
            e.cause.providedClass == Integer
            e.cause.supportedClasses == [String] as HashSet
    }

    @Requires(property = 'spec.name', value = 'SchemaVerificationSpec#1')
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

            startContext(schema, ['spec.name': 'SchemaVerificationSpec#2'])

        when:
            def result = executeQuery("""
{
    hello
}
""")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectMappingException
            e.cause.message == """The field `currentUser` is mapped to built-in class class java.lang.Integer, but required custom Java class
  GraphQL type: User
  GraphQL field: currentUser
  Mapped class: io.micronaut.graphql.tools.SchemaVerificationSpec\$Query2
  Mapped method name: currentUser
  Provided class: java.lang.Integer"""
            e.cause.graphQlTypeName == 'User'
            e.cause.graphQlFieldName == 'currentUser'
            e.cause.mappedClass == Query2
            e.cause.mappedMethodName == 'currentUser'
            e.cause.providedClass == Integer
    }

    @Requires(property = 'spec.name', value = 'SchemaVerificationSpec#2')
    @GraphQLRootResolver
    static class Query2 {
        Integer currentUser() {
            return 0
        }
    }

}
