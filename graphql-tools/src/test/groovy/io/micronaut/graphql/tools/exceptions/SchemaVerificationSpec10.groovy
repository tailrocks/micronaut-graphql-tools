package io.micronaut.graphql.tools.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver
import org.intellij.lang.annotations.Language

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
            e.cause.message == """The class ${SchemaVerificationSpec10.User4.name} is not introspected. Ensure the class is annotated with ${GraphQLType.name}.
  GraphQL type: Query
  GraphQL field: user
  Mapped class: ${SchemaVerificationSpec10.Query4.name}
  Mapped method: user()"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'user'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec10.Query4
            e.cause.mappingDetails.mappedMethod == "user()"
    }

    void "test root resolver returns interface which implementation class is not marked correctly with annotation"() {
        when:
            startContext(SCHEMA, SPEC_NAME_1)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof ImplementationNotFoundException
            e.cause.message == """Can not find implementation class for the interface ${SchemaVerificationSpec10.User1.name}.
  GraphQL type: Query
  GraphQL field: user
  Mapped class: ${SchemaVerificationSpec10.Query1.name}
  Mapped method: user()"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'user'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec10.Query1
            e.cause.mappingDetails.mappedMethod == "user()"
    }

    void "test root resolver returns interface which is not implemented in introspected class"() {
        when:
            startContext(SCHEMA, SPEC_NAME_2)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectImplementationException
            e.cause.message == """The annotated implementation class is not implementing the ${SchemaVerificationSpec10.User2.name} interface.
  GraphQL type: Query
  GraphQL field: user
  Mapped class: ${SchemaVerificationSpec10.Query2.name}
  Mapped method: user()
  Implementation class: ${SchemaVerificationSpec10.User2Impl.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'user'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec10.Query2
            e.cause.mappingDetails.mappedMethod == "user()"
            e.cause.implementationClass == SchemaVerificationSpec10.User2Impl
    }

    void "test root resolver returns interface which has multiple introspected implementation classes"() {
        when:
            startContext(SCHEMA, SPEC_NAME_3)
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof MultipleImplementationsFoundException
            e.cause.message == """Found multiple implementations for the interface ${SchemaVerificationSpec10.User3.name}.
  GraphQL type: Query
  GraphQL field: user
  Mapped class: ${SchemaVerificationSpec10.Query3.name}
  Mapped method: user()
  Implementation classes: ${SchemaVerificationSpec10.User3AltImpl.name}, ${SchemaVerificationSpec10.User3Impl.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'user'
            e.cause.mappingDetails.mappedClass == SchemaVerificationSpec10.Query3
            e.cause.mappingDetails.mappedMethod == "user()"
            e.cause.implementationClasses == [SchemaVerificationSpec10.User3AltImpl, SchemaVerificationSpec10.User3Impl]
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_4)
    @GraphQLRootResolver
    static class Query4 {
        SchemaVerificationSpec10.User4 user() {
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
        SchemaVerificationSpec10.User1 user() {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    static interface User1 {

    }

    @GraphQLType
    static class User1Impl implements SchemaVerificationSpec10.User1 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_1)
    @GraphQLTypeResolver(SchemaVerificationSpec10.User1.class)
    static class User1Resolver {
        String username(SchemaVerificationSpec10.User1 user) {
            return null
        }
    }


    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLRootResolver
    static class Query2 {
        SchemaVerificationSpec10.User2 user() {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    static interface User2 {

    }

    @GraphQLType(SchemaVerificationSpec10.User2)
    static class User2Impl {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_2)
    @GraphQLTypeResolver(SchemaVerificationSpec10.User2.class)
    static class User2Resolver {
        String username(SchemaVerificationSpec10.User2 user) {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_3)
    @GraphQLRootResolver
    static class Query3 {
        SchemaVerificationSpec10.User3 user() {
            return null
        }
    }

    @Requires(property = 'spec.name', value = SPEC_NAME_3)
    static interface User3 {

    }

    @GraphQLType(SchemaVerificationSpec10.User3)
    static class User3Impl implements SchemaVerificationSpec10.User3 {

    }

    @GraphQLType(SchemaVerificationSpec10.User3)
    static class User3AltImpl implements SchemaVerificationSpec10.User3 {

    }

    @Requires(property = 'spec.name', value = SPEC_NAME_3)
    @GraphQLTypeResolver(SchemaVerificationSpec10.User2.class)
    static class User3Resolver {
        String username(SchemaVerificationSpec10.User2 user) {
            return null
        }
    }

}
