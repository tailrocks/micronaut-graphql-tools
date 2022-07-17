package io.micronaut.graphql.tools


import spock.lang.Specification

class SchemaMappingDictionarySpec extends Specification {

    void "test interface"() {
        when:
            new SchemaMappingDictionary().registerType("User", User)

        then:
            def e = thrown(IllegalArgumentException)
            e.message == "${User} must be a top level class."
    }

    void "test duplicate"() {
        when:
            new SchemaMappingDictionary()
                    .registerType("PaymentMethod", PaymentMethod)
                    .registerType("PaymentMethod", PaymentMethod)

        then:
            def e = thrown(IllegalArgumentException)
            e.message == "Duplicated GraphQL type: PaymentMethod"
    }

    void "test conflict"() {
        when:
            new SchemaMappingDictionary()
                    .registerType("PaymentMethod", PaymentMethod)
                    .registerType("User", PaymentMethod)

        then:
            def e = thrown(IllegalArgumentException)
            e.message == "One GraphQL type can have only one implementation class, found duplicate: ${PaymentMethod.name}"
    }

    static interface User {
        String getUsername()
    }

    static class PaymentMethod {
        String cardholder
        String cardNumber
    }

}