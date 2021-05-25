package com.example.graphql.model;

import com.example.graphql.api.PaymentMethod;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLType;

/**
 * @author Alexey Zhokhov
 */
@GraphQLType(PaymentMethod.class)
public class PaymentMethodImpl implements PaymentMethod {

    private final String number;

    public PaymentMethodImpl(String number) {
        this.number = number;
    }

    @Override
    public String getNumber() {
        return number;
    }

}
