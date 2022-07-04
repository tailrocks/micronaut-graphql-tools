package io.micronaut.graphql.tools

import graphql.schema.GraphQLSchema
import spock.lang.Specification

class GraphQLSchemaProviderSpec extends Specification {

    void "test no GraphQLSchema present"() {
        given:
            def provider = new GraphQLSchemaProvider()

        when:
            provider.get()

        then:
            def e = thrown(NoSuchElementException)
            e.message == 'No graphQLSchema present'
    }

    void "test GraphQLSchema present"() {
        given:
            def provider = new GraphQLSchemaProvider()
            provider.init(Mock(GraphQLSchema))

        when:
            def schema = provider.get()

        then:
            schema != null
    }

}
