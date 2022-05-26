package io.micronaut.graphql.tools

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.annotation.GraphQLParameterized
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import io.micronaut.graphql.tools.exceptions.ClassNotIntrospectedException
import io.micronaut.graphql.tools.exceptions.CustomTypeMappedToBuiltInClassException
import io.micronaut.graphql.tools.exceptions.ImplementationNotFoundException
import io.micronaut.graphql.tools.exceptions.IncorrectArgumentCountException
import io.micronaut.graphql.tools.exceptions.IncorrectBuiltInScalarMappingException
import io.micronaut.graphql.tools.exceptions.IncorrectImplementationException
import io.micronaut.graphql.tools.exceptions.InvalidSourceArgumentException
import io.micronaut.graphql.tools.exceptions.MethodNotFoundException
import io.micronaut.graphql.tools.exceptions.MultipleImplementationsFoundException
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
            e.cause.message == 'The method `hello` not found in any GraphQL query resolvers.'
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
            e.cause.message == 'The method `hello` not found in any GraphQL query resolvers.'
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
  Mapped class: ${Query.name}
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
  Mapped class: ${Query.name}
  Mapped method: currentUser
  Provided class: ${Integer.name}"""
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

    void "test method in the root resolver has one argument when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 1, required 0 arg(s).
  GraphQL type: Query
  GraphQL field: username
  Mapped class: ${Query1.name}
  Mapped method: username(${String.name} uid)"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == Query1
            e.cause.mappedMethod == "username(${String.name} uid)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 0
    }

    void "test method in the root resolver has one argument (exclude DataFetchingEnvironment) when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 1, required 0 arg(s).
  GraphQL type: Query
  GraphQL field: username
  Mapped class: ${Query2.name}
  Mapped method: username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == Query2
            e.cause.mappedMethod == "username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"
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

    void "test method in the root resolver has zero arguments when GraphQL schema has one"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 0, required 1 arg(s): (ID uid)
  GraphQL type: Query
  GraphQL field: username
  Mapped class: ${Query1.name}
  Mapped method: username()"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == Query1
            e.cause.mappedMethod == 'username()'
            e.cause.providedCount == 0
            e.cause.requiredCount == 1
    }

    void "test method in the root resolver has zero arguments (exclude DataFetchingEnvironment) when GraphQL schema has one"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 0, required 1 arg(s): (ID uid)
  GraphQL type: Query
  GraphQL field: username
  Mapped class: ${Query2.name}
  Mapped method: username(${DataFetchingEnvironment.name} dfe)"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == Query2
            e.cause.mappedMethod == "username(${DataFetchingEnvironment.name} dfe)"
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

    static final String SPEC_NAME_1 = "SchemaVerificationSpec6_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec6_2"

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

    void "test method in the model has one argument when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 1, required 0 arg(s).
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User1.name}
  Mapped method: username(${String.name} uid)"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == User1
            e.cause.mappedMethod == "username(${String.name} uid)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 0
    }

    void "test method in the model has one argument (exclude DataFetchingEnvironment) when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 1, required 0 arg(s).
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User2.name}
  Mapped method: username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == User2
            e.cause.mappedMethod == "username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 0
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        User1 user() {
            return null
        }
    }

    @GraphQLType
    static class User1 {
        @GraphQLParameterized
        String username(String uid) {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        User2 user() {
            return null
        }
    }

    @GraphQLType
    static class User2 {
        @GraphQLParameterized
        String username(String uid, DataFetchingEnvironment dfe) {
            return null
        }
    }
}

class SchemaVerificationSpec7 extends AbstractTest {

    static final String SPEC_NAME_1 = "SchemaVerificationSpec7_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec7_2"

    @Language("GraphQL")
    static final String SCHEMA = """
schema {
  query: Query
}

type Query {
  user: User
}

type User {
  username(uid: ID): String
}
"""

    void "test method in the type resolver has zero arguments when GraphQL schema has one"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 1, required 2 arg(s): (${User1.name} user, ID uid)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User1Resolver.name}
  Mapped method: username(${User1.name} user)"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == User1Resolver
            e.cause.mappedMethod == "username(${User1.name} user)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 2
    }

    void "test method in the type resolver has zero arguments (exclude DataFetchingEnvironment) when GraphQL schema has one"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 1, required 2 arg(s): (${User2.name} user, ID uid)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User2Resolver.name}
  Mapped method: username(${User2.name} user, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == User2Resolver
            e.cause.mappedMethod == "username(${User2.name} user, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 2
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        User1 user() {
            return null
        }
    }

    @GraphQLType
    static class User1 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLTypeResolver(User1.class)
    static class User1Resolver {
        String username(User1 user) {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        User2 user() {
            return null
        }
    }

    @GraphQLType
    static class User2 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLTypeResolver(User2.class)
    static class User2Resolver {
        String username(User2 user, DataFetchingEnvironment dfe) {
            return null
        }
    }

}

class SchemaVerificationSpec8 extends AbstractTest {

    static final String SPEC_NAME_1 = "SchemaVerificationSpec8_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec8_2"

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

    void "test method in the type resolver has one argument when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 2, required 1 arg(s): (${User1.name} user)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User1Resolver.name}
  Mapped method: username(${User1.name} user, ${String.name} uid)"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == User1Resolver
            e.cause.mappedMethod == "username(${User1.name} user, ${String.name} uid)"
            e.cause.providedCount == 2
            e.cause.requiredCount == 1
    }

    void "test method in the type resolver has one argument (exclude DataFetchingEnvironment) when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 2, required 1 arg(s): (${User2.name} user)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User2Resolver.name}
  Mapped method: username(${User2.name} user, ${String.name} uid, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == User2Resolver
            e.cause.mappedMethod == "username(${User2.name} user, ${String.name} uid, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedCount == 2
            e.cause.requiredCount == 1
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        User1 user() {
            return null
        }
    }

    @GraphQLType
    static class User1 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLTypeResolver(User1.class)
    static class User1Resolver {
        String username(User1 user, String uid) {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        User2 user() {
            return null
        }
    }

    @GraphQLType
    static class User2 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLTypeResolver(User2.class)
    static class User2Resolver {
        String username(User2 user, String uid, DataFetchingEnvironment dfe) {
            return null
        }
    }

}

class SchemaVerificationSpec9 extends AbstractTest {

    static final String SPEC_NAME_1 = "SchemaVerificationSpec9_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec9_2"

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

    void "test first argument in GraphQlType method is source instance"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof InvalidSourceArgumentException
            e.cause.message == """The source argument must be instance of ${User1.name} class, provided: ${String.name}.
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User1Resolver.name}
  Mapped method: username(${String.name} uid)"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == User1Resolver
            e.cause.mappedMethod == "username(${String.name} uid)"
            e.cause.providedClass == String
            e.cause.requiredClass == User1
    }

    void "test first argument in GraphQlType method is source instance (exclude DataFetchingEnvironment)"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof InvalidSourceArgumentException
            e.cause.message == """The source argument must be instance of ${User2.name} class, provided: ${String.name}.
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User2Resolver.name}
  Mapped method: username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'username'
            e.cause.mappedClass == User2Resolver
            e.cause.mappedMethod == "username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedClass == String
            e.cause.requiredClass == User2
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        User1 user() {
            return null
        }
    }

    @GraphQLType
    static class User1 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLTypeResolver(User1.class)
    static class User1Resolver {
        String username(String uid) {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        User2 user() {
            return null
        }
    }

    @GraphQLType
    static class User2 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLTypeResolver(User2.class)
    static class User2Resolver {
        String username(String uid, DataFetchingEnvironment dfe) {
            return null
        }
    }

}

class SchemaVerificationSpec10 extends AbstractTest {

    static final String SPEC_NAME_4 = "SchemaVerificationSpec10_4"
    static final String SPEC_NAME_1 = "SchemaVerificationSpec10_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec10_2"
    static final String SPEC_NAME_3 = "SchemaVerificationSpec10_3"

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
            startContext(SCHEMA, SPEC_NAME_4)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof ClassNotIntrospectedException
            e.cause.message == """The class ${User4.name} is not introspected. Ensure the class is annotated with ${GraphQLType.name}.
  GraphQL type: Query
  GraphQL field: user
  Mapped class: ${Query4.name}
  Mapped method: user"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'user'
            e.cause.mappedClass == Query4
            e.cause.mappedMethod == "user"
    }

    void "test root resolver returns interface which implementation class is not marked correctly with annotation"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof ImplementationNotFoundException
            e.cause.message == """Can not find implementation class for the interface ${User1.name}.
  GraphQL type: Query
  GraphQL field: user
  Mapped class: ${Query1.name}
  Mapped method: user"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'user'
            e.cause.mappedClass == Query1
            e.cause.mappedMethod == "user"
    }

    void "test root resolver returns interface which is not implemented in introspected class"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectImplementationException
            e.cause.message == """The annotated implementation class is not implementing the ${User2.name} interface.
  GraphQL type: Query
  GraphQL field: user
  Mapped class: ${Query2.name}
  Mapped method: user
  Implementation class: ${User2Impl.name}"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'user'
            e.cause.mappedClass == Query2
            e.cause.mappedMethod == "user"
            e.cause.implementationClass == User2Impl
    }

    void "test root resolver returns interface which has multiple introspected implementation classes"() {
        when:
            startContext(SCHEMA, SPEC_NAME_3)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MultipleImplementationsFoundException
            e.cause.message == """Found multiple implementations for the interface ${User3.name}.
  GraphQL type: Query
  GraphQL field: user
  Mapped class: ${Query3.name}
  Mapped method: user
  Implementation classes: ${User3AltImpl.name}, ${User3Impl.name}"""
            e.cause.graphQlType == 'Query'
            e.cause.graphQlField == 'user'
            e.cause.mappedClass == Query3
            e.cause.mappedMethod == "user"
            e.cause.implementationClasses == [User3AltImpl, User3Impl]
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_4)
    @GraphQLRootResolver
    static class Query4 {
        User4 user() {
            return null
        }
    }

    static class User4 {
        String username() {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        User1 user() {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    static interface User1 {

    }

    @GraphQLType
    static class User1Impl implements User1 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLTypeResolver(User1.class)
    static class User1Resolver {
        String username(User1 user) {
            return null
        }
    }


    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        User2 user() {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    static interface User2 {

    }

    @GraphQLType(User2)
    static class User2Impl {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLTypeResolver(User2.class)
    static class User2Resolver {
        String username(User2 user) {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_3)
    @GraphQLRootResolver
    static class Query3 {
        User3 user() {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_3)
    static interface User3 {

    }

    @GraphQLType(User3)
    static class User3Impl implements User3 {

    }

    @GraphQLType(User3)
    static class User3AltImpl implements User3 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_3)
    @GraphQLTypeResolver(User2.class)
    static class User3Resolver {
        String username(User2 user) {
            return null
        }
    }

}

// TODO
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
            e.cause instanceof CustomTypeMappedToBuiltInClassException
            e.cause.message == """The field is mapped to built-in class, but required custom Java class
  GraphQL type: User
  GraphQL field: currentUser
  Mapped class: ${SchemaVerificationSpec6.name}\$${Query5.simpleName}
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
        User1 user() {
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
    @GraphQLTypeResolver(User1)
    static class User1Resolver {
        String gravatar(User1 user) {
            return null
        }
    }

}

class SchemaVerificationSpec12 extends AbstractTest {

    static final String SPEC_NAME_1 = "SchemaVerificationSpec12_1"
    static final String SPEC_NAME_2 = "SchemaVerificationSpec12_2"
    static final String SPEC_NAME_3 = "SchemaVerificationSpec12_3"
    static final String SPEC_NAME_4 = "SchemaVerificationSpec12_4"

    void "test TODO1"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  month: Month
}

enum Month {
  JANUARY
  FEBRUARY
  MARCH
}
"""

            startContext(schema, SPEC_NAME_1)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof CustomTypeMappedToBuiltInClassException
            e.cause.message == """The field is mapped to built-in class, but required custom Java class
  GraphQL type: User
  GraphQL field: currentUser
  Mapped class: ${SchemaVerificationSpec6.name}\$${Query5.simpleName}
  Mapped method name: currentUser
  Provided class: ${Integer.name}"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'currentUser'
            e.cause.mappedClass == Query2
            e.cause.mappedMethod == 'currentUser'
            e.cause.providedClass == Integer
    }

    void "test TODO2"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  displayName(value: Month!): String
}

enum Month {
  JANUARY
  FEBRUARY
  MARCH
}
"""

            startContext(schema, SPEC_NAME_2)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof CustomTypeMappedToBuiltInClassException
            e.cause.message == """The field is mapped to built-in class, but required custom Java class
  GraphQL type: User
  GraphQL field: currentUser
  Mapped class: ${SchemaVerificationSpec6.name}\$${Query5.simpleName}
  Mapped method name: currentUser
  Provided class: ${Integer.name}"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'currentUser'
            e.cause.mappedClass == Query2
            e.cause.mappedMethod == 'currentUser'
            e.cause.providedClass == Integer
    }

    void "test TODO3"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  month: Month
}

enum Month {
  JANUARY
  FEBRUARY
  MARCH
}
"""

            startContext(schema, SPEC_NAME_3)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof CustomTypeMappedToBuiltInClassException
            e.cause.message == """The field is mapped to built-in class, but required custom Java class
  GraphQL type: User
  GraphQL field: currentUser
  Mapped class: ${SchemaVerificationSpec6.name}\$${Query5.simpleName}
  Mapped method name: currentUser
  Provided class: ${Integer.name}"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'currentUser'
            e.cause.mappedClass == Query2
            e.cause.mappedMethod == 'currentUser'
            e.cause.providedClass == Integer
    }

    void "test TODO4"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  currentMonth: Month
  nextMonth: Month
}

enum Month {
  JANUARY
  FEBRUARY
  MARCH
}
"""

            startContext(schema, SPEC_NAME_4)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof CustomTypeMappedToBuiltInClassException
            e.cause.message == """The field is mapped to built-in class, but required custom Java class
  GraphQL type: User
  GraphQL field: currentUser
  Mapped class: ${SchemaVerificationSpec6.name}\$${Query5.simpleName}
  Mapped method name: currentUser
  Provided class: ${Integer.name}"""
            e.cause.graphQlType == 'User'
            e.cause.graphQlField == 'currentUser'
            e.cause.mappedClass == Query2
            e.cause.mappedMethod == 'currentUser'
            e.cause.providedClass == Integer
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLRootResolver
    static class Query1 {
        MyMonth month() {
            return null
        }
    }

    @GraphQLType
    static class MyMonth {
        String getJanuary() {
            return "JAN"
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        String displayName(String value) {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_3)
    @GraphQLRootResolver
    static class Query3 {
        Month3 month() {
            return null
        }
    }

    static enum Month3 {
        JANUARY,
        FEBRUARY
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_4)
    @GraphQLRootResolver
    static class Query4 {
        Month4 currentMonth() {
            return null
        }
        AnotherMonth4 nextMonth() {
            return null
        }
    }

    static enum Month4 {
        JANUARY,
        FEBRUARY,
        MARCH
    }

    static enum AnotherMonth4 {
        JANUARY,
        FEBRUARY,
        MARCH
    }

}
