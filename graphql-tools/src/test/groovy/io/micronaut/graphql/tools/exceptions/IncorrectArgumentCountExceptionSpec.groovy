package io.micronaut.graphql.tools.exceptions

import graphql.schema.DataFetchingEnvironment
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLParameterized
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import org.intellij.lang.annotations.Language

class IncorrectArgumentCountExceptionSpec1 extends AbstractTest {

    static final String SPEC_NAME_1 = "IncorrectArgumentCountExceptionSpec1_1"
    static final String SPEC_NAME_2 = "IncorrectArgumentCountExceptionSpec1_2"

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
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == Query1
            e.cause.mappingDetails.mappedMethod == "username(${String.name} uid)"
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
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == Query2
            e.cause.mappingDetails.mappedMethod == "username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"
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


class IncorrectArgumentCountExceptionSpec2 extends AbstractTest {

    static final String SPEC_NAME_1 = "IncorrectArgumentCountExceptionSpec2_1"
    static final String SPEC_NAME_2 = "IncorrectArgumentCountExceptionSpec2_2"

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
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == Query1
            e.cause.mappingDetails.mappedMethod == 'username()'
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
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == Query2
            e.cause.mappingDetails.mappedMethod == "username(${DataFetchingEnvironment.name} dfe)"
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


class IncorrectArgumentCountExceptionSpec3 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectArgumentCountExceptionSpec3"

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
            startContext(SCHEMA, SPEC_NAME)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 1, required 0 arg(s).
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User.name}
  Mapped method: username(${String.name} uid)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == User
            e.cause.mappingDetails.mappedMethod == "username(${String.name} uid)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 0
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return null
        }
    }

    @GraphQLType
    static class User {
        @GraphQLParameterized
        String username(String uid) {
            return null
        }
    }

}


class IncorrectArgumentCountExceptionSpec4 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectArgumentCountExceptionSpec6"

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

    void "test method in the model has one argument (exclude DataFetchingEnvironment) when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 1, required 0 arg(s).
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User.name}
  Mapped method: username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == User
            e.cause.mappingDetails.mappedMethod == "username(${String.name} uid, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 0
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return null
        }
    }

    @GraphQLType
    static class User {
        @GraphQLParameterized
        String username(String uid, DataFetchingEnvironment dfe) {
            return null
        }
    }

}


class IncorrectArgumentCountExceptionSpec5 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectArgumentCountExceptionSpec4"

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
            startContext(SCHEMA, SPEC_NAME)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 1, required 2 arg(s): (${User.name} user, ID uid)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${UserResolver.name}
  Mapped method: username(${User.name} user)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == UserResolver
            e.cause.mappingDetails.mappedMethod == "username(${User.name} user)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 2
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return null
        }
    }

    @GraphQLType
    static class User {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(User.class)
    static class UserResolver {
        String username(User user) {
            return null
        }
    }

}


class IncorrectArgumentCountExceptionSpec6 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectArgumentCountExceptionSpec7"

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

    void "test method in the type resolver has zero arguments (exclude DataFetchingEnvironment) when GraphQL schema has one"() {
        when:
            startContext(SCHEMA, SPEC_NAME)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too few arguments, provided: 1, required 2 arg(s): (${User.name} user, ID uid)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${UserResolver.name}
  Mapped method: username(${User.name} user, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == UserResolver
            e.cause.mappingDetails.mappedMethod == "username(${User.name} user, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedCount == 1
            e.cause.requiredCount == 2
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return null
        }
    }

    @GraphQLType
    static class User {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(User.class)
    static class UserResolver {
        String username(User user, DataFetchingEnvironment dfe) {
            return null
        }
    }

}


class IncorrectArgumentCountExceptionSpec7 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectArgumentCountExceptionSpec5"

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
            startContext(SCHEMA, SPEC_NAME)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 2, required 1 arg(s): (${User.name} user)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${UserResolver.name}
  Mapped method: username(${User.name} user, ${String.name} uid)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == UserResolver
            e.cause.mappingDetails.mappedMethod == "username(${User.name} user, ${String.name} uid)"
            e.cause.providedCount == 2
            e.cause.requiredCount == 1
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return null
        }
    }

    @GraphQLType
    static class User {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(User.class)
    static class UserResolver {
        String username(User user, String uid) {
            return null
        }
    }

}


class IncorrectArgumentCountExceptionSpec8 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectArgumentCountExceptionSpec8"

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

    void "test method in the type resolver has one argument (exclude DataFetchingEnvironment) when GraphQL schema has zero"() {
        when:
            startContext(SCHEMA, SPEC_NAME)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectArgumentCountException
            e.cause.message == """The method has too many arguments, provided: 2, required 1 arg(s): (${User.name} user)
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${UserResolver.name}
  Mapped method: username(${User.name} user, ${String.name} uid, ${DataFetchingEnvironment.name} dfe)"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == UserResolver
            e.cause.mappingDetails.mappedMethod == "username(${User.name} user, ${String.name} uid, ${DataFetchingEnvironment.name} dfe)"
            e.cause.providedCount == 2
            e.cause.requiredCount == 1
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return null
        }
    }

    @GraphQLType
    static class User {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLTypeResolver(User.class)
    static class UserResolver {
        String username(User user, String uid, DataFetchingEnvironment dfe) {
            return null
        }
    }

}
