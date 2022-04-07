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

}
