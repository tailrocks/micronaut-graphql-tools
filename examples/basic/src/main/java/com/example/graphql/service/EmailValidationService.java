package com.example.graphql.service;

import javax.inject.Singleton;

/**
 * @author Alexey Zhokhov
 */
@Singleton
public class EmailValidationService {

    public boolean isValid(String email) {
        return true;
    }

}
