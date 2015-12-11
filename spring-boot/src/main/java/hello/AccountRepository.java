package hello;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class AccountRepository {
    private final static Logger LOG = LoggerFactory.getLogger(AccountRepository.class);

    AccountRepository() {
        setUser1();
    }



    public final Map<String, User> userDatabase = new HashMap<>();

    public void addUser(User user) {
        userDatabase.put(user.getUsername(), user);
    }

    public User getUser(String username) {
        return userDatabase.get(username);
    }

    private void setUser1() {
        final User value = new User();
        value.setPassword("secret");
        userDatabase.put("henk", value);
    }
}
