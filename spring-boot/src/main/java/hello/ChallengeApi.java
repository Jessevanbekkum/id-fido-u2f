package hello;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yubico.u2f.U2F;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.AuthenticateRequestData;
import com.yubico.u2f.data.messages.AuthenticateResponse;
import com.yubico.u2f.data.messages.RegisterRequestData;
import com.yubico.u2f.data.messages.RegisterResponse;
import com.yubico.u2f.exceptions.DeviceCompromisedException;
import com.yubico.u2f.exceptions.NoEligableDevicesException;

@RestController
public class ChallengeApi {
    private final static Logger LOG = LoggerFactory.getLogger(ChallengeApi.class);
    public static final String APP_ID = "https://localhost:8443";

    Map<String, String> requestStorage = new HashMap<>();
    Map<String, AuthenticateRequestData> challengeStore = new HashMap<>();
    private final U2F u2f = new U2F();

    private final LoadingCache<String, Map<String, String>> userStorage = CacheBuilder.newBuilder().build(new CacheLoader<String, Map<String, String>>() {
        @Override
        public Map<String, String> load(String key) throws Exception {
            return new HashMap<>();
        }
    });

    @RequestMapping(path = "yubi/register/{username}", method = RequestMethod.GET
    )
    public RegisterRequestData startRegistration(@PathVariable String username) {
        RegisterRequestData registerRequestData = u2f.startRegistration(APP_ID, getRegistrations(username));
        requestStorage.put(registerRequestData.getRequestId(), registerRequestData.toJson());
        return registerRequestData;

    }

    @RequestMapping(path = "yubi/register/{username}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String finishRegistration(@RequestBody String registerResponseStr, @PathVariable("username") String username) {
        System.out.println(registerResponseStr);

        final RegisterResponse registerResponse = RegisterResponse.fromJson(registerResponseStr);

        System.out.println("registerResponse - " + registerResponse);
        RegisterRequestData registerRequestData =
                RegisterRequestData.fromJson(requestStorage.remove(registerResponse.getRequestId()));
        DeviceRegistration registration = u2f.finishRegistration(registerRequestData, registerResponse);
        addRegistration(username, registration);
        return "{}";
    }


    @RequestMapping(value = "/yubi/auth/{username}", method = RequestMethod.GET)
    public String startAuthentication(@PathVariable("username") String username) throws NoEligableDevicesException {
        System.out.println("startAuthentication: " + username);
        AuthenticateRequestData authenticateRequestData = u2f.startAuthentication(APP_ID, getRegistrations(username));
        requestStorage.put(authenticateRequestData.getRequestId(), authenticateRequestData.toJson());
        return authenticateRequestData.toJson();
    }

    @RequestMapping(value = "/yubi/auth/{username}", method = RequestMethod.POST)
    public String finishAuthentication(@RequestBody String response, @PathVariable String username) {
        AuthenticateResponse authenticateResponse = AuthenticateResponse.fromJson(response);
        AuthenticateRequestData authenticateRequest = AuthenticateRequestData.fromJson(requestStorage.remove(authenticateResponse.getRequestId()));
        DeviceRegistration registration = null;
        try {
            registration = u2f.finishAuthentication(authenticateRequest, authenticateResponse, getRegistrations(username));
        } catch (DeviceCompromisedException e) {
            registration = e.getDeviceRegistration();
            return "<p>Device possibly compromised and therefore blocked: " + e.getMessage() + "</p>";
        } finally {
            userStorage.getUnchecked(username).put(registration.getKeyHandle(), registration.toJson());
        }

        grantAuthority();

        return "{}";
    }


    private Iterable<DeviceRegistration> getRegistrations(String username) {
        List<DeviceRegistration> registrations = new ArrayList<>();
        for (String serialized : userStorage.getUnchecked(username).values()) {
            registrations.add(DeviceRegistration.fromJson(serialized));
        }
        return registrations;
    }

    private void addRegistration(String username, DeviceRegistration registration) {
        System.out.println("Username - " + username);
        System.out.println("DeviceRegistration - " + registration);
        userStorage.getUnchecked(username).put(registration.getKeyHandle(), registration.toJson());
    }

    private void grantAuthority() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> authorities = new ArrayList<>(auth.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

}

