package com.wsllclabs.memorialops.support;

public final class LoginRequestBuilder {

    private String username = TestUsers.COORDINATOR.username();
    private String password = TestUsers.COORDINATOR.password();

    public static LoginRequestBuilder forUser(String username) {
        return new LoginRequestBuilder().username(username);
    }

    public LoginRequestBuilder username(String username) {
        this.username = username;
        return this;
    }

    public LoginRequestBuilder password(String password) {
        this.password = password;
        return this;
    }

    public String json() {
        return "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password);
    }
}
