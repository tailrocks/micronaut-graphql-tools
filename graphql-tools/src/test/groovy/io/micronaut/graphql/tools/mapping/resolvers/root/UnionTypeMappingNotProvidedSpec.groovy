package io.micronaut.graphql.tools.mapping.resolvers.root

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.SchemaParserDictionary
import io.micronaut.graphql.tools.SchemaParserDictionaryCustomizer
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.exceptions.UnionTypeMappingNotProvidedException
import org.intellij.lang.annotations.Language

class UnionTypeMappingNotProvidedSpec extends AbstractTest {

    static final String SPEC_NAME = "mapping.resolvers.root.UnionTypeMappingNotProvidedSpec"

    @Language("GraphQL")
    static final String SCHEMA = """
schema {
  query: Query
}

type Query {
  unionTest: PayloadError
}

union PayloadError = SecurityError | ValidationError

type SecurityError {
  code: String!
}

type ValidationError {
  code: Int!
}
"""

    void "test unable to detect representation class for type member of union"() {
        given:
            startContext(SCHEMA, SPEC_NAME)

        when:
            getGraphQLBean()

        then:
            def e = thrown(BeanInstantiationException)
            e.cause instanceof UnionTypeMappingNotProvidedException
            e.cause.message == """Can not detect representation class for type ValidationError, member of PayloadError union. Ensure the representation class is registered via ${SchemaParserDictionary.name}.
  GraphQL type: Query
  GraphQL field: unionTest
  Mapped class: ${Query.name}
  Mapped method: unionTest()"""
            e.cause.mappingContext.graphQlType == 'Query'
            e.cause.mappingContext.graphQlField == 'unionTest'
            e.cause.mappingContext.mappedClass == Query
            e.cause.mappingContext.mappedMethod == 'unionTest()'
    }

    @CompileStatic
    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        PayloadError unionTest() {
            return null
        }
    }

    @CompileStatic
    static interface PayloadError {
    }

    @CompileStatic
    @GraphQLType
    static class SecurityError implements PayloadError {
        String code = "AUTH"
    }

    @CompileStatic
    @GraphQLType
    static class ValidationError implements PayloadError {
        Integer code = 123
    }

    @CompileStatic
    @Requires(property = 'spec.name', value = SPEC_NAME)
    @io.micronaut.context.annotation.Factory
    static class GraphQLFactory {
        @Bean
        @jakarta.inject.Singleton
        SchemaParserDictionaryCustomizer schemaParserDictionaryCustomizer() {
            return new SchemaParserDictionaryCustomizer() {
                @Override
                void customize(SchemaParserDictionary schemaParserDictionary) {
                    schemaParserDictionary.registerType("SecurityError", SecurityError.class)
                }
            }
        }

    }

}