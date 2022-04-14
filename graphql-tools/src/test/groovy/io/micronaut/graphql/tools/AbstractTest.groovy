package io.micronaut.graphql.tools

import io.micronaut.context.ApplicationContext
import spock.lang.Specification

abstract class AbstractTest extends Specification {

    protected ApplicationContext applicationContext

    protected void startContext(Map additionalConfig = [:]) {
        applicationContext = ApplicationContext.run(
                ["spec.name": getClass().simpleName] << additionalConfig,
                "test"
        )
    }

    void cleanup() {
        applicationContext?.close()
    }

}
