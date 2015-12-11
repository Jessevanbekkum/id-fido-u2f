package hello;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        registerEndpoints();
    }

    private void registerEndpoints() {
        register(ChallengeApi.class);
    }
}
