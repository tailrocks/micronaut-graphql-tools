package io.micronaut.graphql.tools

import graphql.GraphQL
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class ApplicationSpec extends Specification {

    @Inject ApplicationContext applicationContext

    void "test initialization"() {
        when:
            applicationContext.getBean(GraphQL.class)

        then:
            true
    }

    void "test DataFetchingEnvironment passed to Query's field"() {
        when:
            def executionResult = applicationContext.getBean(GraphQL.class).execute("""
{ 
    userSignedIn {
        email
    }
}
""")

        then:
            executionResult.errors.isEmpty()
            executionResult.dataPresent
            executionResult.data["userSignedIn"]["email"] == "me@test.com"
    }

    void "test passing arguments to graphql type fields"() {
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

}
