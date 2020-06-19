package com.google.sps;

public class AuthenticationResponse {
    private boolean isLoggedIn;
    private String email;
    private String logInUrl;
    private String logOutUrl;

    public AuthenticationResponse(boolean isLoggedIn, String email, String logInUrl, String logOutUrl) {
        this.isLoggedIn = isLoggedIn;
        this.email = email;
        this.logInUrl = logInUrl;
        this.logOutUrl = logOutUrl;
    }
}