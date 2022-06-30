package io.micronaut.graphql.tools.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import org.intellij.lang.annotations.Language

class IncorrectClassMappingExceptionSpec1 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectClassMappingExceptionSpec1"

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
            executeQuery('{hello}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the incorrect class.
  GraphQL type: Query
  GraphQL field: hello
  Mapped class: ${Query.name}
  Mapped method: hello()
  Provided class: ${Integer.name}
  Supported classes: ${String.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'hello'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == 'hello()'
            e.cause.providedClass == Integer
            e.cause.supportedClasses == [String] as HashSet
    }

    void "test mapping built-in GraphQL type to a wrong class [required field]"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  hello: String!
}
"""

            startContext(schema, SPEC_NAME)

        when:
            executeQuery('{hello}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the incorrect class.
  GraphQL type: Query
  GraphQL field: hello
  Mapped class: ${Query.name}
  Mapped method: hello()
  Provided class: ${Integer.name}
  Supported classes: ${String.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'hello'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == 'hello()'
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


class IncorrectClassMappingExceptionSpec2 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectClassMappingExceptionSpec2"

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
            executeQuery('{hello}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the built-in class, when required custom Java class.
  GraphQL type: Query
  GraphQL field: currentUser
  Mapped class: ${Query.name}
  Mapped method: currentUser()
  Provided class: ${Integer.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'currentUser'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == 'currentUser()'
            e.cause.providedClass == Integer
    }

    void "test mapping custom GraphQL type to a wrong class [required field]"() {
        given:
            @Language("GraphQL")
            String schema = """
schema {
  query: Query
}

type Query {
  currentUser: User!
}

type User {
  username: String
}
"""

            startContext(schema, SPEC_NAME)

        when:
            executeQuery('{hello}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the built-in class, when required custom Java class.
  GraphQL type: Query
  GraphQL field: currentUser
  Mapped class: ${Query.name}
  Mapped method: currentUser()
  Provided class: ${Integer.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'currentUser'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == 'currentUser()'
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


class IncorrectClassMappingExceptionSpec3 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectClassMappingExceptionSpec3"

    void "test GraphQL field inside sub-type mapped to the incorrect class"() {
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
            executeQuery('{hello}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the incorrect class.
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User.name}
  Mapped method: getUsername()
  Provided class: ${Integer.name}
  Supported classes: ${String.name}"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == User
            e.cause.mappingDetails.mappedMethod == 'getUsername()'
            e.cause.providedClass == Integer
            e.cause.supportedClasses == [String] as HashSet
    }

    void "test GraphQL field inside sub-type mapped to the incorrect class [required field]"() {
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
}
"""

            startContext(schema, SPEC_NAME)

        when:
            executeQuery('{hello}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the incorrect class.
  GraphQL type: User
  GraphQL field: username
  Mapped class: ${User.name}
  Mapped method: getUsername()
  Provided class: ${Integer.name}
  Supported classes: ${String.name}"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.mappingDetails.mappedClass == User
            e.cause.mappingDetails.mappedMethod == 'getUsername()'
            e.cause.providedClass == Integer
            e.cause.supportedClasses == [String] as HashSet
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
        Integer getUsername() {
            return 0
        }
    }

}


class IncorrectClassMappingExceptionSpec4 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectClassMappingExceptionSpec4"

    void "test GraphQL schema enum mapped to a Java class"() {
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

            startContext(schema, SPEC_NAME)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the class, when required Enum.
  GraphQL type: Query
  GraphQL field: month
  Mapped class: ${Query.name}
  Mapped method: month()
  Provided class: ${MyMonth.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'month'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == 'month()'
            e.cause.providedClass == MyMonth
    }


    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
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

}


class IncorrectClassMappingExceptionSpec5 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectClassMappingExceptionSpec5"

    void "test GraphQL schema enum as a input parameter mapped to a Java class"() {
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

            startContext(schema, SPEC_NAME)

        when:
            executeQuery("{month}")

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the class, when required Enum.
  GraphQL type: Query
  GraphQL field: displayName
  Mapped class: ${IncorrectClassMappingExceptionSpec5.name}\$${Query.simpleName}
  Mapped method: displayName(java.lang.String value)
  Provided class: java.lang.String"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'displayName'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == 'displayName(java.lang.String value)'
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        String displayName(String value) {
            return null
        }
    }

}


class IncorrectClassMappingExceptionSpec6 extends AbstractTest {

    static final String SPEC_NAME = "IncorrectClassMappingExceptionSpec6"

    void "test enum annotated with GraphQLType instead of custom class"() {
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
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to the enum, when required custom Java class.
  GraphQL type: User
  GraphQL field: username
  Provided class: ${User.name}"""
            e.cause.mappingDetails.graphQlType == 'User'
            e.cause.mappingDetails.graphQlField == 'username'
            e.cause.providedClass == User
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        User user() {
            return null
        }
    }

    @GraphQLType
    static enum User {
        TEST
    }

}
