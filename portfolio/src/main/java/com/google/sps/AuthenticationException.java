package com.google.sps;

import java.io.IOException;

/**
 * Represents an authentication exception
 */
public class AuthenticationException extends IOException {
    public AuthenticationException(String message) {
        super(message);
    }
}