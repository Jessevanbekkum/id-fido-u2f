package com.xebia.fido_u2f;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String username;
    private String password;
    private final Map<String, String> devices = new HashMap<>();

    public User(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void putDevice(final String keyHandle, final String registration) {
        devices.put(keyHandle, registration);
    }

    public String getDevice(final String keyHandle) {
        return devices.get(keyHandle);
    }

    public Collection<String> getRegistrations() {
        return devices.values();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("username='").append(username).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
