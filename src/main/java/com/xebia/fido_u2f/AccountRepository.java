package com.xebia.fido_u2f;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A simple repository for users. Supports adding users, and retrieving them by username. In any real application a database
 * would be used. Passwords are not hashed and the repository is initialized with three users.
 */

@Component
public class AccountRepository {
    private final static Logger LOG = LoggerFactory.getLogger(AccountRepository.class);

    private final Map<String, User> userDatabase = new HashMap<>();

    public AccountRepository() {
        setUser1();
        setUser2();
        setUser3();
    }

    public void addUser(User user) {
        userDatabase.put(user.getUsername(), user);
    }

    public User getUser(String username) {
        return userDatabase.get(username);
    }

    private void setUser1() {
        final User value = new User("alan", "password");
        userDatabase.put("alan", value);
    }

    private void setUser2() {
        final User value = new User("charles", "password");
        userDatabase.put("charles", value);
    }

    private void setUser3() {
        final User value = new User("edsger", "password");
        userDatabase.put("edsger", value);
    }
}
