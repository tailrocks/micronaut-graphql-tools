package com.example.graphql.api;

import graphql.schema.DataFetchingEnvironment;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @author Alexey Zhokhov
 */
public interface UserResolver {

    CompletionStage<List<PaymentMethod>> paymentMethodList(User user, DataFetchingEnvironment env) throws Exception;

}
