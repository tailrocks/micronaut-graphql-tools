package io.micronaut.graphql.tools

import graphql.GraphQL
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.core.io.ResourceResolver
import jakarta.inject.Singleton

class CatalogSectionTopListSpec extends AbstractTest {

    void "test passing arguments to graphql type fields"() {
        given:
            startContext()

        when:
            def executionResult = applicationContext.getBean(GraphQL.class).execute("""
{ 
    catalogSectionTopList {
        data {
            slug
            name
            icon {
                url
                width
                height
            }
            description3: description(maxlength: 3)
            overview(prefix: "Pre", limit: { 
                start: 1
                end: 5
            })
        }
    }
}
""")

        then:
            executionResult.errors.isEmpty()
            executionResult.dataPresent
            executionResult.data["catalogSectionTopList"]["data"]

            executionResult.data["catalogSectionTopList"]["data"][0].slug == "abc"
            executionResult.data["catalogSectionTopList"]["data"][0].name == "ABC"
            executionResult.data["catalogSectionTopList"]["data"][0].icon.url == "http://google.com"
            executionResult.data["catalogSectionTopList"]["data"][0].icon.width == 150
            executionResult.data["catalogSectionTopList"]["data"][0].icon.height == 100
            executionResult.data["catalogSectionTopList"]["data"][0].description3 == "a-c"
            executionResult.data["catalogSectionTopList"]["data"][0].overview == "Preaabb"

            executionResult.data["catalogSectionTopList"]["data"][1].slug == "xyz"
            executionResult.data["catalogSectionTopList"]["data"][1].name == "XYZ"
            executionResult.data["catalogSectionTopList"]["data"][1].icon == null
            executionResult.data["catalogSectionTopList"]["data"][1].description3 == "x-z"
            executionResult.data["catalogSectionTopList"]["data"][1].overview == "Prexxyy"
    }

    @Requires(property = "spec.name", value = "CatalogSectionTopListSpec")
    @Factory
    static class CustomGraphQLFactory {

        @Bean
        @Singleton
        TypeDefinitionRegistry typeDefinitionRegistry(ResourceResolver resourceResolver) {
            SchemaParser schemaParser = new SchemaParser()

            TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry()
            typeRegistry.merge(schemaParser.parse(new BufferedReader(new InputStreamReader(
                    resourceResolver.getResourceAsStream("classpath:graphql/example.graphqls").get()))))

            return typeRegistry
        }

    }

}
