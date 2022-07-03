package io.micronaut.graphql.tools.mapping.resolvers.root

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanContextException
import io.micronaut.graphql.tools.AbstractTest
import io.micronaut.graphql.tools.SchemaParserDictionary
import io.micronaut.graphql.tools.SchemaParserDictionaryCustomizer
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver
import io.micronaut.graphql.tools.annotation.GraphQLType
import io.micronaut.graphql.tools.exceptions.IncorrectAnnotationException
import org.intellij.lang.annotations.Language

class Union2Spec extends AbstractTest {

    static final String SPEC_NAME = "mapping.resolvers.root.Union2Spec"

    @Language("GraphQL")
    static final String SCHEMA = """
schema {
  query: Query
}

type Query {
  unionTypeTest: PayloadError
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
            executeQuery("""
{
    unionTypeTest(securityError: true) {
        ... on SecurityError {
            securityCode: code
        }
        ... on ValidationError {
            validationCode: code
        }
    }
}
""")

        then:
            def e = thrown(BeanContextException)
            e.cause instanceof IncorrectAnnotationException
            e.cause.message == "Empty value member for @GraphQLTypeResolver annotation in ${UserResolver.class.name} class."
    }

    @CompileStatic
    @Requires(property = 'spec.name', value = SPEC_NAME)
    @GraphQLRootResolver
    static class Query {
        PayloadError unionTypeTest() {
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
                    schemaParserDictionary.addType("SecurityError", SecurityError.class)
                }
            }
        }

    }

}
