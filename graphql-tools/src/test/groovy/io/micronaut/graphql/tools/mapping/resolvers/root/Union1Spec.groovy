package io.micronaut.graphql.tools.mapping.resolvers.root

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.SchemaParserDictionaryCustomizer
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.exceptions.IncorrectClassMappingException
import org.intellij.lang.annotations.Language

class Union1Spec extends AbstractTest {

    static final String SPEC_NAME = "mapping.resolvers.root.Union1Spec"

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
        given:
            startContext(SCHEMA, SPEC_NAME)

        when:
            executeQuery('{username}')

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof IncorrectClassMappingException
            e.cause.message == """The field is mapped to a custom class, when required an interface.
  GraphQL type: Query
  GraphQL field: unionTypeTest
  Mapped class: ${Query.name}
  Mapped method: unionTypeTest(boolean securityError)
  Provided class: ${SecurityError.name}"""
            e.cause.mappingDetails.graphQlType == 'Query'
            e.cause.mappingDetails.graphQlField == 'unionTypeTest'
            e.cause.mappingDetails.mappedClass == Query
            e.cause.mappingDetails.mappedMethod == 'unionTypeTest(boolean securityError)'
            e.cause.providedClass == SecurityError
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        SecurityError unionTypeTest(boolean securityError) {
            return null
        }
    }

    static interface PayloadError {
    }

    @GraphQLType
    static class SecurityError implements PayloadError {
        String code = "AUTH"
    }

    @GraphQLType
    static class ValidationError implements PayloadError {
        Integer code = 123
    }

    @Requires(property = 'spec.name', value = SPEC_NAME)
    @io.micronaut.context.annotation.Factory
    static class GraphQLFactory {
        @Bean
        @jakarta.inject.Singleton
        SchemaParserDictionaryCustomizer schemaParserDictionaryCustomizer() {
            return (schemaParserDictionary) -> schemaParserDictionary
                    .addType("SecurityError", SecurityError.class)
                    .addType("ValidationError", ValidationError.class)
        }

    }

}
